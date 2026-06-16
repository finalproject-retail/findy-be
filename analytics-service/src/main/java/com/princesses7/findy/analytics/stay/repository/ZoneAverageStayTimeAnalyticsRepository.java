package com.princesses7.findy.analytics.stay.repository;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeDailyQueryResult;
import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeQueryResult;
import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeSummaryQueryResult;

@Repository
public class ZoneAverageStayTimeAnalyticsRepository {

	private static final int MAX_STAY_SECONDS = 30 * 60;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public ZoneAverageStayTimeAnalyticsRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ZoneAverageStayTimeSummaryQueryResult findSummary(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds
	) {
		String sql = """
			WITH inferred_logs AS (
				SELECT
					user_id,
					zone_id,
					CASE
						WHEN stay_duration_seconds BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN stay_duration_seconds

						WHEN exited_at IS NOT NULL
							AND exited_at >= entered_at
							AND EXTRACT(EPOCH FROM (exited_at - entered_at)) BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT

						ELSE NULL
					END AS stay_seconds
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND zone_id IS NOT NULL
			), valid_logs AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds IS NOT NULL
					AND (:zoneId IS NULL OR zone_id = :zoneId)
			)
			SELECT
				COUNT(*) AS total_visit_count,
				COUNT(DISTINCT user_id) AS total_unique_visitor_count,
				COALESCE(ROUND(AVG(stay_seconds))::BIGINT, 0) AS average_stay_duration_seconds,
				COALESCE(SUM(stay_seconds), 0)::BIGINT AS total_stay_duration_seconds
			FROM valid_logs
			""";

		return jdbcTemplate.queryForObject(
			sql,
			params(periodRange, storeId, zoneId, minStaySeconds, null),
			(rs, rowNum) -> new ZoneAverageStayTimeSummaryQueryResult(
				rs.getLong("total_visit_count"),
				rs.getLong("total_unique_visitor_count"),
				rs.getLong("average_stay_duration_seconds"),
				rs.getLong("total_stay_duration_seconds")
			)
		);
	}

	public List<ZoneAverageStayTimeDailyQueryResult> findDailyTrends(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds
	) {
		String sql = """
			WITH inferred_logs AS (
				SELECT
					user_id,
					zone_id,
					entered_at::DATE AS analysis_date,
					CASE
						WHEN stay_duration_seconds BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN stay_duration_seconds

						WHEN exited_at IS NOT NULL
							AND exited_at >= entered_at
							AND EXTRACT(EPOCH FROM (exited_at - entered_at)) BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT

						ELSE NULL
					END AS stay_seconds
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND zone_id IS NOT NULL
			), valid_logs AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds IS NOT NULL
					AND (:zoneId IS NULL OR zone_id = :zoneId)
			)
			SELECT
				analysis_date,
				COUNT(*) AS visit_count,
				COUNT(DISTINCT user_id) AS unique_visitor_count,
				COALESCE(ROUND(AVG(stay_seconds))::BIGINT, 0) AS average_stay_duration_seconds,
				COALESCE(SUM(stay_seconds), 0)::BIGINT AS total_stay_duration_seconds
			FROM valid_logs
			GROUP BY analysis_date
			ORDER BY analysis_date ASC
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, minStaySeconds, null),
			(rs, rowNum) -> new ZoneAverageStayTimeDailyQueryResult(
				rs.getDate("analysis_date").toLocalDate(),
				rs.getLong("visit_count"),
				rs.getLong("unique_visitor_count"),
				rs.getLong("average_stay_duration_seconds"),
				rs.getLong("total_stay_duration_seconds")
			)
		);
	}

	public List<ZoneAverageStayTimeQueryResult> findZoneAverageStayTimes(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds,
		int limit
	) {
		String sql = """
			WITH inferred_logs AS (
				SELECT
					user_id,
					zone_id,
					zone_name,
					CASE
						WHEN stay_duration_seconds BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN stay_duration_seconds

						WHEN exited_at IS NOT NULL
							AND exited_at >= entered_at
							AND EXTRACT(EPOCH FROM (exited_at - entered_at)) BETWEEN :minStaySeconds AND :maxStaySeconds
						THEN EXTRACT(EPOCH FROM (exited_at - entered_at))::BIGINT

						ELSE NULL
					END AS stay_seconds
				FROM user_location_logs
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND store_id = :storeId
					AND zone_id IS NOT NULL
			), valid_logs AS (
				SELECT *
				FROM inferred_logs
				WHERE stay_seconds IS NOT NULL
					AND (:zoneId IS NULL OR zone_id = :zoneId)
			), zone_stats AS (
				SELECT
					zone_id,
					MAX(zone_name) AS zone_name,
					COUNT(*) AS visit_count,
					COUNT(DISTINCT user_id) AS unique_visitor_count,
					ROUND(AVG(stay_seconds))::BIGINT AS average_stay_duration_seconds,
					COALESCE(SUM(stay_seconds), 0)::BIGINT AS total_stay_duration_seconds,
					MIN(stay_seconds)::BIGINT AS min_stay_duration_seconds,
					MAX(stay_seconds)::BIGINT AS max_stay_duration_seconds
				FROM valid_logs
				GROUP BY zone_id
			), ranked_zone_stats AS (
				SELECT
					*,
					RANK() OVER (
						ORDER BY average_stay_duration_seconds DESC, total_stay_duration_seconds DESC, zone_id ASC
					)::INTEGER AS rank_no
				FROM zone_stats
			)
			SELECT
				zone_id,
				zone_name,
				visit_count,
				unique_visitor_count,
				average_stay_duration_seconds,
				total_stay_duration_seconds,
				min_stay_duration_seconds,
				max_stay_duration_seconds,
				rank_no
			FROM ranked_zone_stats
			ORDER BY rank_no ASC, zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, minStaySeconds, limit),
			(rs, rowNum) -> new ZoneAverageStayTimeQueryResult(
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getLong("visit_count"),
				rs.getLong("unique_visitor_count"),
				rs.getLong("average_stay_duration_seconds"),
				rs.getLong("total_stay_duration_seconds"),
				rs.getLong("min_stay_duration_seconds"),
				rs.getLong("max_stay_duration_seconds"),
				rs.getInt("rank_no")
			)
		);
	}

	private MapSqlParameterSource params(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int minStaySeconds,
		Integer limit
	) {
		return new MapSqlParameterSource()
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("zoneId", zoneId, Types.BIGINT)
			.addValue("minStaySeconds", minStaySeconds, Types.INTEGER)
			.addValue("maxStaySeconds", MAX_STAY_SECONDS, Types.INTEGER)
			.addValue("limit", limit, Types.INTEGER);
	}
}