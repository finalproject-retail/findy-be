package com.princesses7.findy.analytics.zone.repository;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.zone.dto.query.ZoneMovementQueryResult;
import com.princesses7.findy.analytics.zone.dto.query.ZoneVisitRateQueryResult;

@Repository
public class ZoneVisitRateAnalyticsRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public ZoneVisitRateAnalyticsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<ZoneVisitRateQueryResult> findZoneVisitRates(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds
	) {
		String sql = """
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					zone_name,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at, location_log_id
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND zone_id IS NOT NULL
			), inferred_logs AS (
				SELECT
					user_id,
					zone_id,
					zone_name,
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
			), valid_visits AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds >= :minStaySeconds
			), total_visits AS (
				SELECT COUNT(*) AS total_visit_count
				FROM valid_visits
			), zone_stats AS (
				SELECT
					zone_id,
					MAX(zone_name) AS zone_name,
					COUNT(*) AS visit_count,
					COUNT(DISTINCT user_id) AS unique_visitor_count,
					ROUND(AVG(stay_seconds))::BIGINT AS average_stay_duration_seconds,
					COALESCE(SUM(stay_seconds), 0)::BIGINT AS total_stay_duration_seconds
				FROM valid_visits
				GROUP BY zone_id
			), ranked_zone_stats AS (
				SELECT
					zone_id,
					zone_name,
					visit_count,
					unique_visitor_count,
					CASE
						WHEN (SELECT total_visit_count FROM total_visits) = 0 THEN 0
						ELSE ROUND(
							visit_count::NUMERIC * 100
							/ (SELECT total_visit_count FROM total_visits),
							2
						)
					END AS visit_rate,
					average_stay_duration_seconds,
					total_stay_duration_seconds,
					RANK() OVER (
						ORDER BY visit_count DESC, total_stay_duration_seconds DESC, zone_id ASC
					)::INTEGER AS rank_no
				FROM zone_stats
			)
			SELECT
				zone_id,
				zone_name,
				visit_count,
				unique_visitor_count,
				visit_rate,
				average_stay_duration_seconds,
				total_stay_duration_seconds,
				rank_no
			FROM ranked_zone_stats
			WHERE (:zoneId IS NULL OR zone_id = :zoneId)
			ORDER BY rank_no ASC, zone_id ASC
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, minStaySeconds),
			(rs, rowNum) -> new ZoneVisitRateQueryResult(
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getLong("visit_count"),
				rs.getLong("unique_visitor_count"),
				rs.getBigDecimal("visit_rate"),
				rs.getLong("average_stay_duration_seconds"),
				rs.getLong("total_stay_duration_seconds"),
				rs.getInt("rank_no")
			)
		);
	}

	public List<ZoneMovementQueryResult> findZoneMovements(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds
	) {
		String sql = """
			WITH ordered_logs AS (
				SELECT
					location_log_id,
					user_id,
					store_id,
					zone_id,
					zone_name,
					entered_at,
					exited_at,
					stay_duration_seconds,
					LEAD(zone_id) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at, location_log_id
					) AS next_zone_id,
					LEAD(zone_name) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at, location_log_id
					) AS next_zone_name,
					LEAD(entered_at) OVER (
						PARTITION BY user_id, store_id
						ORDER BY entered_at, location_log_id
					) AS next_entered_at
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND zone_id IS NOT NULL
			), inferred_logs AS (
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
			), valid_movements AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds >= :minStaySeconds
			), total_movements AS (
				SELECT COUNT(*) AS total_movement_count
				FROM valid_movements
			), movement_stats AS (
				SELECT
					from_zone_id,
					MAX(from_zone_name) AS from_zone_name,
					to_zone_id,
					MAX(to_zone_name) AS to_zone_name,
					COUNT(*) AS movement_count,
					ROUND(AVG(travel_seconds))::BIGINT AS average_travel_time_seconds
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
			WHERE (:zoneId IS NULL OR from_zone_id = :zoneId OR to_zone_id = :zoneId)
			ORDER BY movement_count DESC, from_zone_id ASC, to_zone_id ASC
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, minStaySeconds),
			(rs, rowNum) -> new ZoneMovementQueryResult(
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

	private MapSqlParameterSource params(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds
	) {
		return new MapSqlParameterSource()
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("zoneId", zoneId, Types.BIGINT)
			.addValue("minStaySeconds", minStaySeconds, Types.INTEGER);
	}
}