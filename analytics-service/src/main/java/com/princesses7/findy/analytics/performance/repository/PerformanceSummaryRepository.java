package com.princesses7.findy.analytics.performance.repository;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.performance.dto.query.PerformanceDailyMetricQueryResult;

@Repository
public class PerformanceSummaryRepository {

	private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final String analyticsSchema;
	private final String sourceSchema;

	public PerformanceSummaryRepository(
		NamedParameterJdbcTemplate jdbcTemplate,
		@Value("${spring.flyway.default-schema:analytics_service}") String analyticsSchema,
		@Value("${analytics.source-schema:shopping_service}") String sourceSchema
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.analyticsSchema = normalizeSchema(analyticsSchema);
		this.sourceSchema = normalizeSchema(sourceSchema);
	}

	public List<PerformanceDailyMetricQueryResult> findDailyMetrics(
		PeriodRange periodRange,
		Long storeId
	) {
		String sql = """
			WITH days AS (
				SELECT generate_series(
					CAST(:startDate AS date),
					CAST(:endDate AS date),
					INTERVAL '1 day'
				)::date AS metric_date
			),
			daily_visitors AS (
				SELECT
					CAST(entered_at AS date) AS metric_date,
					COUNT(DISTINCT user_id) AS visitor_count
				FROM %s
				WHERE entered_at >= :fromAt
					AND entered_at < :toAt
					AND (:storeId IS NULL OR store_id = :storeId)
				GROUP BY CAST(entered_at AS date)
			),
			daily_stockouts AS (
				SELECT
					analysis_date AS metric_date,
					COALESCE(SUM(stockout_count), 0) AS out_of_stock_count
				FROM %s
				WHERE analysis_date >= :startDate
					AND analysis_date <= :endDate
					AND (:storeId IS NULL OR store_id = :storeId)
				GROUP BY analysis_date
			),
			daily_routes AS (
				SELECT
					CAST(used_at AS date) AS metric_date,
					COUNT(*) AS route_usage_count
				FROM %s
				WHERE used_at >= :fromAt
					AND used_at < :toAt
					AND (:storeId IS NULL OR store_id = :storeId)
				GROUP BY CAST(used_at AS date)
			),
			daily_recommendations AS (
				SELECT
					CAST(created_at AS date) AS metric_date,
					COUNT(*) AS recommendation_exposure_count,
					COALESCE(SUM(CASE WHEN is_purchased = TRUE THEN 1 ELSE 0 END), 0) AS recommendation_purchase_count
				FROM %s
				WHERE created_at >= :fromAt
					AND created_at < :toAt
				GROUP BY CAST(created_at AS date)
			),
			daily_sales AS (
				SELECT
					CAST(o.created_at AS date) AS metric_date,
					COALESCE(SUM(o.final_amount), 0) AS total_sales_amount,
					COUNT(DISTINCT o.order_id) AS total_order_count
				FROM %s o
				WHERE o.created_at >= :fromAt
					AND o.created_at < :toAt
					AND o.order_status = 'COMPLETED'
					AND (
						:storeId IS NULL
						OR EXISTS (
							SELECT 1
							FROM %s oi
							JOIN %s i ON i.product_id = oi.product_id
							WHERE oi.order_id = o.order_id
								AND i.store_id = :storeId
						)
					)
				GROUP BY CAST(o.created_at AS date)
			)
			SELECT
				d.metric_date,
				COALESCE(v.visitor_count, 0) AS visitor_count,
				COALESCE(s.out_of_stock_count, 0) AS out_of_stock_count,
				COALESCE(r.route_usage_count, 0) AS route_usage_count,
				COALESCE(rec.recommendation_exposure_count, 0) AS recommendation_exposure_count,
				COALESCE(rec.recommendation_purchase_count, 0) AS recommendation_purchase_count,
				COALESCE(sales.total_sales_amount, 0) AS total_sales_amount,
				COALESCE(sales.total_order_count, 0) AS total_order_count
			FROM days d
			LEFT JOIN daily_visitors v ON v.metric_date = d.metric_date
			LEFT JOIN daily_stockouts s ON s.metric_date = d.metric_date
			LEFT JOIN daily_routes r ON r.metric_date = d.metric_date
			LEFT JOIN daily_recommendations rec ON rec.metric_date = d.metric_date
			LEFT JOIN daily_sales sales ON sales.metric_date = d.metric_date
			ORDER BY d.metric_date ASC
			""".formatted(
			analyticsTable("user_location_logs"),
			analyticsTable("stockout_analytics"),
			analyticsTable("route_usage_logs"),
			analyticsTable("recommendation_logs"),
			sourceTable("orders"),
			sourceTable("order_items"),
			sourceTable("inventories")
		);

		return jdbcTemplate.query(
			sql,
			params(periodRange, storeId),
			(rs, rowNum) -> new PerformanceDailyMetricQueryResult(
				toLocalDate(rs.getDate("metric_date")),
				rs.getLong("visitor_count"),
				rs.getLong("out_of_stock_count"),
				rs.getLong("route_usage_count"),
				rs.getLong("recommendation_exposure_count"),
				rs.getLong("recommendation_purchase_count"),
				rs.getLong("total_sales_amount"),
				rs.getLong("total_order_count")
			)
		);
	}

	private MapSqlParameterSource params(
		PeriodRange periodRange,
		Long storeId
	) {
		return new MapSqlParameterSource()
			.addValue("startDate", periodRange.startDate(), Types.DATE)
			.addValue("endDate", periodRange.endDate(), Types.DATE)
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("storeId", storeId, Types.BIGINT);
	}

	private LocalDate toLocalDate(java.sql.Date date) {
		return date == null ? null : date.toLocalDate();
	}

	private String analyticsTable(String tableName) {
		if (analyticsSchema.isBlank()) {
			return tableName;
		}

		return analyticsSchema + "." + tableName;
	}

	private String sourceTable(String tableName) {
		if (sourceSchema.isBlank()) {
			return tableName;
		}

		return sourceSchema + "." + tableName;
	}

	private String normalizeSchema(String schema) {
		if (schema == null || schema.isBlank()) {
			return "";
		}

		if (!SAFE_SCHEMA_PATTERN.matcher(schema).matches()) {
			throw new IllegalArgumentException("올바르지 않은 스키마명입니다.");
		}

		return schema;
	}
}