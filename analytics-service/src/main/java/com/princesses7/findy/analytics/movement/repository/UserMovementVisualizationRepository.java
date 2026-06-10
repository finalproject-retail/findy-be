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
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					zone_name,
					grid_id,
					grid_x,
					grid_y,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			inferred_logs AS (
				SELECT
					location_log_id,
					user_id,
					zone_id,
					COALESCE(zone_name, CONCAT('구역 ', zone_id)) AS zone_name,
					grid_id,
					grid_x,
					grid_y,
					entered_at,
					exited_at,
					GREATEST(
						0,
						COALESCE(
							stay_duration_seconds,
							EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
							EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
							0
						)
					) AS inferred_stay_duration_seconds
				FROM ordered_logs
			),
			valid_logs AS (
				SELECT *
				FROM inferred_logs
				WHERE inferred_stay_duration_seconds >= :minStaySeconds
			),
			target_users AS (
				SELECT user_id
				FROM valid_logs
				GROUP BY user_id
				ORDER BY MIN(entered_at) ASC, user_id ASC
				LIMIT :limit
			)
			SELECT
				v.user_id,
				v.location_log_id,
				v.zone_id,
				v.zone_name,
				v.grid_id,
				v.grid_x,
				v.grid_y,
				v.entered_at,
				v.exited_at,
				v.inferred_stay_duration_seconds AS stay_duration_seconds,
				ROW_NUMBER() OVER (
					PARTITION BY v.user_id
					ORDER BY v.entered_at ASC, v.location_log_id ASC
				)::INTEGER AS sequence_no
			FROM valid_logs v
			JOIN target_users tu ON tu.user_id = v.user_id
			ORDER BY v.user_id ASC, v.entered_at ASC, v.location_log_id ASC
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, limit),
			(rs, rowNum) -> new UserMovementPointQueryResult(
				rs.getLong("user_id"),
				rs.getLong("location_log_id"),
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getObject("grid_id", Long.class),
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
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					zone_name,
					grid_id,
					grid_x,
					grid_y,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			inferred_logs AS (
				SELECT
					user_id,
					zone_id,
					COALESCE(zone_name, CONCAT('구역 ', zone_id)) AS zone_name,
					grid_id,
					grid_x,
					grid_y,
					GREATEST(
						0,
						COALESCE(
							stay_duration_seconds,
							EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
							EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
							0
						)
					) AS inferred_stay_duration_seconds
				FROM ordered_logs
			),
			valid_logs AS (
				SELECT *
				FROM inferred_logs
				WHERE inferred_stay_duration_seconds >= :minStaySeconds
			)
			SELECT
				zone_id,
				MAX(zone_name) AS zone_name,
				MAX(grid_id) AS grid_id,
				ROUND(AVG(grid_x))::INTEGER AS grid_x,
				ROUND(AVG(grid_y))::INTEGER AS grid_y,
				COUNT(*)::BIGINT AS visit_count,
				COUNT(DISTINCT user_id)::BIGINT AS unique_visitor_count,
				COALESCE(SUM(inferred_stay_duration_seconds), 0)::BIGINT AS total_stay_duration_seconds,
				COALESCE(ROUND(AVG(inferred_stay_duration_seconds)), 0)::BIGINT AS average_stay_duration_seconds
			FROM valid_logs
			GROUP BY zone_id
			ORDER BY visit_count DESC, total_stay_duration_seconds DESC, zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, limit),
			(rs, rowNum) -> new UserMovementHeatmapQueryResult(
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getObject("grid_id", Long.class),
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
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					COALESCE(zone_name, CONCAT('구역 ', zone_id)) AS zone_name,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(zone_id) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_zone_id,
					LEAD(COALESCE(zone_name, CONCAT('구역 ', zone_id))) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_zone_name,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			inferred_logs AS (
				SELECT
					zone_id AS from_zone_id,
					zone_name AS from_zone_name,
					next_zone_id AS to_zone_id,
					next_zone_name AS to_zone_name,
					GREATEST(
						0,
						COALESCE(
							stay_duration_seconds,
							EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
							EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
							0
						)
					) AS stay_seconds,
					GREATEST(
						0,
						EXTRACT(EPOCH FROM (
							next_entered_at - COALESCE(
								exited_at,
								CASE
									WHEN stay_duration_seconds IS NOT NULL
									THEN entered_at + (stay_duration_seconds * INTERVAL '1 second')
									ELSE entered_at
								END
							)
						))::BIGINT
					) AS travel_seconds
				FROM ordered_logs
				WHERE next_zone_id IS NOT NULL
					AND next_entered_at IS NOT NULL
					AND zone_id <> next_zone_id
			),
			valid_movements AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds >= :minStaySeconds
			),
			total_movements AS (
				SELECT COUNT(*)::BIGINT AS total_movement_count
				FROM valid_movements
			),
			movement_stats AS (
				SELECT
					from_zone_id,
					MAX(from_zone_name) AS from_zone_name,
					to_zone_id,
					MAX(to_zone_name) AS to_zone_name,
					COUNT(*)::BIGINT AS movement_count,
					COALESCE(ROUND(AVG(travel_seconds)), 0)::BIGINT AS average_travel_time_seconds
				FROM valid_movements
				GROUP BY from_zone_id, to_zone_id
			)
			SELECT
				from_zone_id,
				from_zone_name,
				to_zone_id,
				to_zone_name,
				movement_count,
				CASE
					WHEN (SELECT total_movement_count FROM total_movements) = 0 THEN 0
					ELSE ROUND(
						movement_count::NUMERIC * 100
						/ (SELECT total_movement_count FROM total_movements),
						2
					)
				END AS movement_rate,
				average_travel_time_seconds
			FROM movement_stats
			ORDER BY movement_count DESC, from_zone_id ASC, to_zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, limit),
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

	public Long countTotalUsers(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds
	) {
		String sql = """
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			valid_logs AS (
				SELECT user_id
				FROM ordered_logs
				WHERE GREATEST(
					0,
					COALESCE(
						stay_duration_seconds,
						EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
						EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
						0
					)
				) >= :minStaySeconds
			)
			SELECT COUNT(DISTINCT user_id)::BIGINT
			FROM valid_logs
			""";

		return jdbcTemplate.queryForObject(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	public Long countTotalVisits(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds
	) {
		String sql = """
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			valid_logs AS (
				SELECT location_log_id
				FROM ordered_logs
				WHERE GREATEST(
					0,
					COALESCE(
						stay_duration_seconds,
						EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
						EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
						0
					)
				) >= :minStaySeconds
			)
			SELECT COUNT(*)::BIGINT
			FROM valid_logs
			""";

		return jdbcTemplate.queryForObject(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	public Long sumTotalStayDuration(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds
	) {
		String sql = """
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at ASC, location_log_id ASC
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND user_id = COALESCE(:userId, user_id)
					AND zone_id IS NOT NULL
			),
			inferred_logs AS (
				SELECT
					GREATEST(
						0,
						COALESCE(
							stay_duration_seconds,
							EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT,
							EXTRACT(EPOCH FROM (next_entered_at - entered_at))::BIGINT,
							0
						)
					) AS stay_seconds
				FROM ordered_logs
			),
			valid_logs AS (
				SELECT stay_seconds
				FROM inferred_logs
				WHERE stay_seconds >= :minStaySeconds
			)
			SELECT COALESCE(SUM(stay_seconds), 0)::BIGINT
			FROM valid_logs
			""";

		return jdbcTemplate.queryForObject(
			sql,
			params(periodRange, storeId, userId, minStaySeconds, 1),
			Long.class
		);
	}

	private MapSqlParameterSource params(
		PeriodRange periodRange,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		return new MapSqlParameterSource()
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("userId", userId, Types.BIGINT)
			.addValue("minStaySeconds", minStaySeconds, Types.INTEGER)
			.addValue("limit", limit, Types.INTEGER);
	}
}