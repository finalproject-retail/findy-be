package com.princesses7.findy.analytics.congestion.repository;

import java.sql.Types;
import java.util.List;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionOverallSummaryQueryResult;
import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionSummaryQueryResult;
import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionTimeSeriesQueryResult;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ZoneCongestionAnalyticsRepository {

	private static final int DEFAULT_OPEN_LOG_SECONDS = 600;
	private static final int CONGESTED_USER_COUNT = 6;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public ZoneCongestionOverallSummaryQueryResult findOverallSummary(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int intervalMinutes
	) {
		String sql = """
			WITH time_slots AS (
				SELECT generate_series(
					:fromAt::TIMESTAMP,
					(:toAt::TIMESTAMP - (:intervalMinutes * INTERVAL '1 minute')),
					(:intervalMinutes * INTERVAL '1 minute')
				) AS time_slot
			),
			target_zones AS (
				SELECT
					ull.zone_id,
					COALESCE(MAX(ull.zone_name), CONCAT('구역 ', ull.zone_id)) AS zone_name
				FROM user_location_logs ull
				WHERE ull.store_id = :storeId
					AND ull.zone_id IS NOT NULL
					AND ull.entered_at < :toAt
					AND COALESCE(
						ull.exited_at,
						ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
					) >= :fromAt
					AND ull.zone_id = COALESCE(:zoneId, ull.zone_id)
				GROUP BY ull.zone_id
			),
			zone_time_slots AS (
				SELECT
					ts.time_slot,
					tz.zone_id,
					tz.zone_name
				FROM time_slots ts
				CROSS JOIN target_zones tz
			),
			slot_counts AS (
				SELECT
					zts.time_slot,
					zts.zone_id,
					zts.zone_name,
					COUNT(DISTINCT ull.user_id)::BIGINT AS user_count
				FROM zone_time_slots zts
				LEFT JOIN user_location_logs ull
					ON ull.store_id = :storeId
					AND ull.zone_id = zts.zone_id
					AND ull.entered_at <= zts.time_slot
					AND COALESCE(
						ull.exited_at,
						ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
					) > zts.time_slot
				GROUP BY zts.time_slot, zts.zone_id, zts.zone_name
			)
			SELECT
				COALESCE(ROUND(AVG(user_count), 2), 0) AS average_user_count,
				COALESCE(MAX(user_count), 0)::BIGINT AS max_user_count,
				COUNT(*) FILTER (WHERE user_count >= :congestedUserCount)::BIGINT AS congested_slot_count,
				COUNT(*)::BIGINT AS total_slot_count
			FROM slot_counts
			""";

		return jdbcTemplate.queryForObject(
			sql,
			params(periodRange, storeId, zoneId, intervalMinutes, null),
			(rs, rowNum) -> new ZoneCongestionOverallSummaryQueryResult(
				rs.getBigDecimal("average_user_count"),
				rs.getLong("max_user_count"),
				rs.getLong("congested_slot_count"),
				rs.getLong("total_slot_count")
			)
		);
	}

	public List<ZoneCongestionSummaryQueryResult> findZoneSummaries(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int intervalMinutes,
		int limit
	) {
		String sql = """
			WITH time_slots AS (
				SELECT generate_series(
					:fromAt::TIMESTAMP,
					(:toAt::TIMESTAMP - (:intervalMinutes * INTERVAL '1 minute')),
					(:intervalMinutes * INTERVAL '1 minute')
				) AS time_slot
			),
			target_zones AS (
				SELECT
					ull.zone_id,
					COALESCE(MAX(ull.zone_name), CONCAT('구역 ', ull.zone_id)) AS zone_name
				FROM user_location_logs ull
				WHERE ull.store_id = :storeId
					AND ull.zone_id IS NOT NULL
					AND ull.entered_at < :toAt
					AND COALESCE(
						ull.exited_at,
						ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
					) >= :fromAt
					AND ull.zone_id = COALESCE(:zoneId, ull.zone_id)
				GROUP BY ull.zone_id
			),
			zone_time_slots AS (
				SELECT
					ts.time_slot,
					tz.zone_id,
					tz.zone_name
				FROM time_slots ts
				CROSS JOIN target_zones tz
			),
			slot_counts AS (
				SELECT
					zts.time_slot,
					zts.zone_id,
					zts.zone_name,
					COUNT(DISTINCT ull.user_id)::BIGINT AS user_count
				FROM zone_time_slots zts
				LEFT JOIN user_location_logs ull
					ON ull.store_id = :storeId
					AND ull.zone_id = zts.zone_id
					AND ull.entered_at <= zts.time_slot
					AND COALESCE(
						ull.exited_at,
						ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
					) > zts.time_slot
				GROUP BY zts.time_slot, zts.zone_id, zts.zone_name
			)
			SELECT
				zone_id,
				zone_name,
				COALESCE(ROUND(AVG(user_count), 2), 0) AS average_user_count,
				COALESCE(MAX(user_count), 0)::BIGINT AS max_user_count,
				COUNT(*) FILTER (WHERE user_count >= :congestedUserCount)::BIGINT AS congested_slot_count,
				COUNT(*)::BIGINT AS total_slot_count
			FROM slot_counts
			GROUP BY zone_id, zone_name
			ORDER BY max_user_count DESC, average_user_count DESC, zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, intervalMinutes, limit),
			(rs, rowNum) -> new ZoneCongestionSummaryQueryResult(
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getBigDecimal("average_user_count"),
				rs.getLong("max_user_count"),
				rs.getLong("congested_slot_count"),
				rs.getLong("total_slot_count")
			)
		);
	}

	public List<ZoneCongestionTimeSeriesQueryResult> findTimeSeries(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int intervalMinutes,
		int limit
	) {
		String sql = """
			WITH time_slots AS (
				SELECT generate_series(
					:fromAt::TIMESTAMP,
					(:toAt::TIMESTAMP - (:intervalMinutes * INTERVAL '1 minute')),
					(:intervalMinutes * INTERVAL '1 minute')
				) AS time_slot
			),
			target_zones AS (
				SELECT
					ull.zone_id,
					COALESCE(MAX(ull.zone_name), CONCAT('구역 ', ull.zone_id)) AS zone_name
				FROM user_location_logs ull
				WHERE ull.store_id = :storeId
					AND ull.zone_id IS NOT NULL
					AND ull.entered_at < :toAt
					AND COALESCE(
						ull.exited_at,
						ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
					) >= :fromAt
					AND ull.zone_id = COALESCE(:zoneId, ull.zone_id)
				GROUP BY ull.zone_id
			),
			zone_time_slots AS (
				SELECT
					ts.time_slot,
					tz.zone_id,
					tz.zone_name
				FROM time_slots ts
				CROSS JOIN target_zones tz
			)
			SELECT
				zts.time_slot,
				zts.zone_id,
				zts.zone_name,
				COUNT(DISTINCT ull.user_id)::BIGINT AS user_count
			FROM zone_time_slots zts
			LEFT JOIN user_location_logs ull
				ON ull.store_id = :storeId
				AND ull.zone_id = zts.zone_id
				AND ull.entered_at <= zts.time_slot
				AND COALESCE(
					ull.exited_at,
					ull.entered_at + COALESCE(ull.stay_duration_seconds, :defaultOpenLogSeconds) * INTERVAL '1 second'
				) > zts.time_slot
			GROUP BY zts.time_slot, zts.zone_id, zts.zone_name
			ORDER BY zts.time_slot ASC, zts.zone_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId, zoneId, intervalMinutes, limit),
			(rs, rowNum) -> new ZoneCongestionTimeSeriesQueryResult(
				rs.getTimestamp("time_slot").toLocalDateTime(),
				rs.getLong("zone_id"),
				rs.getString("zone_name"),
				rs.getLong("user_count")
			)
		);
	}

	private MapSqlParameterSource params(
		PeriodRange periodRange,
		Long storeId,
		Long zoneId,
		int intervalMinutes,
		Integer limit
	) {
		return new MapSqlParameterSource()
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("zoneId", zoneId, Types.BIGINT)
			.addValue("intervalMinutes", intervalMinutes, Types.INTEGER)
			.addValue("limit", limit, Types.INTEGER)
			.addValue("defaultOpenLogSeconds", DEFAULT_OPEN_LOG_SECONDS, Types.INTEGER)
			.addValue("congestedUserCount", CONGESTED_USER_COUNT, Types.INTEGER);
	}
}