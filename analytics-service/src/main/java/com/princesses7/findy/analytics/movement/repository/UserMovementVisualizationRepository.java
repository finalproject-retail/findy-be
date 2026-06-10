package com.princesses7.findy.analytics.movement.repository;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementFlowQueryResult;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementHeatmapQueryResult;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementPointQueryResult;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserMovementVisualizationRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public List<UserMovementPointQueryResult> findMovementPoints(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		String sql = """
			WITH filtered_logs AS (
				SELECT
					ull.location_log_id,
					ull.user_id,
					ull.zone_id,
					COALESCE(sz.zone_name, CONCAT('구역 ', ull.zone_id)) AS zone_name,
					ull.grid_id,
					g.grid_x,
					g.grid_y,
					ull.entered_at,
					ull.exited_at,
					COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) AS stay_duration_seconds
				FROM analytics_service.user_location_logs ull
				LEFT JOIN shopping_service.store_zones sz
					ON sz.zone_id = ull.zone_id
				LEFT JOIN shopping_service.grids g
					ON g.grid_id = ull.grid_id
				WHERE ull.store_id = :storeId
					AND ull.entered_at >= :startedAt
					AND ull.entered_at < :endedAt
					AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
					AND COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) >= :minStaySeconds
			),
			numbered_logs AS (
				SELECT
					location_log_id,
					user_id,
					zone_id,
					zone_name,
					grid_id,
					grid_x,
					grid_y,
					entered_at,
					exited_at,
					stay_duration_seconds,
					ROW_NUMBER() OVER (
						PARTITION BY user_id
						ORDER BY entered_at ASC, location_log_id ASC
					)::INTEGER AS sequence_no
				FROM filtered_logs
			)
			SELECT
				user_id,
				location_log_id,
				zone_id,
				zone_name,
				grid_id,
				grid_x,
				grid_y,
				entered_at,
				exited_at,
				stay_duration_seconds,
				sequence_no
			FROM numbered_logs
			ORDER BY user_id ASC, entered_at ASC, location_log_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, limit),
			(rs, rowNum) -> new UserMovementPointQueryResult(
				rs.getLong("user_id"),
				rs.getLong("location_log_id"),
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getLong("grid_id"),
				rs.getObject("grid_x", Integer.class),
				rs.getObject("grid_y", Integer.class),
				rs.getTimestamp("entered_at").toLocalDateTime(),
				rs.getTimestamp("exited_at") == null ? null : rs.getTimestamp("exited_at").toLocalDateTime(),
				rs.getLong("stay_duration_seconds"),
				rs.getInt("sequence_no")
			)
		);
	}

	public List<UserMovementHeatmapQueryResult> findHeatmaps(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		String sql = """
			WITH filtered_logs AS (
				SELECT
					ull.user_id,
					ull.zone_id,
					COALESCE(sz.zone_name, CONCAT('구역 ', ull.zone_id)) AS zone_name,
					ull.grid_id,
					g.grid_x,
					g.grid_y,
					COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) AS stay_duration_seconds
				FROM analytics_service.user_location_logs ull
				LEFT JOIN shopping_service.store_zones sz
					ON sz.zone_id = ull.zone_id
				LEFT JOIN shopping_service.grids g
					ON g.grid_id = ull.grid_id
				WHERE ull.store_id = :storeId
					AND ull.entered_at >= :startedAt
					AND ull.entered_at < :endedAt
					AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
					AND COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) >= :minStaySeconds
			)
			SELECT
				zone_id,
				zone_name,
				grid_id,
				grid_x,
				grid_y,
				COUNT(*)::BIGINT AS visit_count,
				COUNT(DISTINCT user_id)::BIGINT AS unique_visitor_count,
				COALESCE(SUM(stay_duration_seconds), 0)::BIGINT AS total_stay_duration_seconds,
				COALESCE(ROUND(AVG(stay_duration_seconds)), 0)::BIGINT AS average_stay_duration_seconds
			FROM filtered_logs
			GROUP BY zone_id, zone_name, grid_id, grid_x, grid_y
			ORDER BY visit_count DESC, total_stay_duration_seconds DESC, zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, limit),
			(rs, rowNum) -> new UserMovementHeatmapQueryResult(
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getLong("grid_id"),
				rs.getObject("grid_x", Integer.class),
				rs.getObject("grid_y", Integer.class),
				rs.getLong("visit_count"),
				rs.getLong("unique_visitor_count"),
				rs.getLong("total_stay_duration_seconds"),
				rs.getLong("average_stay_duration_seconds")
			)
		);
	}

	public List<UserMovementFlowQueryResult> findFlows(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		String sql = """
			WITH filtered_logs AS (
				SELECT
					ull.location_log_id,
					ull.user_id,
					ull.zone_id,
					COALESCE(sz.zone_name, CONCAT('구역 ', ull.zone_id)) AS zone_name,
					ull.entered_at,
					ull.exited_at,
					COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) AS stay_duration_seconds
				FROM analytics_service.user_location_logs ull
				LEFT JOIN shopping_service.store_zones sz
					ON sz.zone_id = ull.zone_id
				WHERE ull.store_id = :storeId
					AND ull.entered_at >= :startedAt
					AND ull.entered_at < :endedAt
					AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
					AND COALESCE(
						ull.stay_duration_seconds,
						CASE
							WHEN ull.exited_at IS NOT NULL
							THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
							ELSE 0
						END
					) >= :minStaySeconds
			),
			ordered_logs AS (
				SELECT
					user_id,
					zone_id,
					zone_name,
					entered_at,
					exited_at,
					LEAD(zone_id) OVER (
						PARTITION BY user_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_zone_id,
					LEAD(zone_name) OVER (
						PARTITION BY user_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_zone_name,
					LEAD(entered_at) OVER (
						PARTITION BY user_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM filtered_logs
			),
			flows AS (
				SELECT
					zone_id AS from_zone_id,
					zone_name AS from_zone_name,
					next_zone_id AS to_zone_id,
					next_zone_name AS to_zone_name,
					COUNT(*)::BIGINT AS movement_count,
					COALESCE(
						ROUND(AVG(EXTRACT(EPOCH FROM (next_entered_at - COALESCE(exited_at, entered_at)))))::BIGINT,
						0
					) AS average_travel_time_seconds
				FROM ordered_logs
				WHERE next_zone_id IS NOT NULL
					AND zone_id <> next_zone_id
				GROUP BY zone_id, zone_name, next_zone_id, next_zone_name
			),
			total_flow AS (
				SELECT COALESCE(SUM(movement_count), 0) AS total_count
				FROM flows
			)
			SELECT
				f.from_zone_id,
				f.from_zone_name,
				f.to_zone_id,
				f.to_zone_name,
				f.movement_count,
				CASE
					WHEN tf.total_count = 0 THEN 0
					ELSE ROUND((f.movement_count::NUMERIC / tf.total_count::NUMERIC) * 100, 2)
				END AS movement_rate,
				f.average_travel_time_seconds
			FROM flows f
			CROSS JOIN total_flow tf
			ORDER BY f.movement_count DESC, f.from_zone_id ASC, f.to_zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, limit),
			(rs, rowNum) -> new UserMovementFlowQueryResult(
				rs.getLong("from_zone_id"),
				rs.getString("from_zone_name"),
				rs.getLong("to_zone_id"),
				rs.getString("to_zone_name"),
				rs.getLong("movement_count"),
				rs.getBigDecimal("movement_rate"),
				rs.getLong("average_travel_time_seconds")
			)
		);
	}

	public Long countTotalUsers(PeriodRange periodRange, Long storeId, Long userId, Integer minStaySeconds) {
		String sql = """
			SELECT COUNT(DISTINCT ull.user_id)::BIGINT
			FROM analytics_service.user_location_logs ull
			WHERE ull.store_id = :storeId
				AND ull.entered_at >= :startedAt
				AND ull.entered_at < :endedAt
				AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
				AND COALESCE(
					ull.stay_duration_seconds,
					CASE
						WHEN ull.exited_at IS NOT NULL
						THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
						ELSE 0
					END
				) >= :minStaySeconds
			""";

		return jdbcTemplate.queryForObject(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	public Long countTotalVisits(PeriodRange periodRange, Long storeId, Long userId, Integer minStaySeconds) {
		String sql = """
			SELECT COUNT(*)::BIGINT
			FROM analytics_service.user_location_logs ull
			WHERE ull.store_id = :storeId
				AND ull.entered_at >= :startedAt
				AND ull.entered_at < :endedAt
				AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
				AND COALESCE(
					ull.stay_duration_seconds,
					CASE
						WHEN ull.exited_at IS NOT NULL
						THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
						ELSE 0
					END
				) >= :minStaySeconds
			""";

		return jdbcTemplate.queryForObject(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	public Long sumTotalStayDuration(PeriodRange periodRange, Long storeId, Long userId, Integer minStaySeconds) {
		String sql = """
			SELECT COALESCE(SUM(
				COALESCE(
					ull.stay_duration_seconds,
					CASE
						WHEN ull.exited_at IS NOT NULL
						THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
						ELSE 0
					END
				)
			), 0)::BIGINT
			FROM analytics_service.user_location_logs ull
			WHERE ull.store_id = :storeId
				AND ull.entered_at >= :startedAt
				AND ull.entered_at < :endedAt
				AND (:userId::BIGINT IS NULL OR ull.user_id = :userId)
				AND COALESCE(
					ull.stay_duration_seconds,
					CASE
						WHEN ull.exited_at IS NOT NULL
						THEN EXTRACT(EPOCH FROM (ull.exited_at - ull.entered_at))::BIGINT
						ELSE 0
					END
				) >= :minStaySeconds
			""";

		return jdbcTemplate.queryForObject(
			sql,
			createBaseParams(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	private MapSqlParameterSource createBaseParams(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("startedAt", periodRange.startedAt())
			.addValue("endedAt", periodRange.endedAt())
			.addValue("storeId", storeId)
			.addValue("minStaySeconds", minStaySeconds)
			.addValue("limit", limit);

		if (userId == null) {
			params.addValue("userId", null, Types.BIGINT);
		} else {
			params.addValue("userId", userId);
		}

		return params;
	}
}