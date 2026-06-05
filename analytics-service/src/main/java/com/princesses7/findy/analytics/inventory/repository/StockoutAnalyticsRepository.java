package com.princesses7.findy.analytics.inventory.repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutDailyQueryResult;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutProductQueryResult;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutSummaryQueryResult;

@Repository
public class StockoutAnalyticsRepository {

	private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final String sourceSchema;

	public StockoutAnalyticsRepository(
		NamedParameterJdbcTemplate jdbcTemplate,
		@Value("${analytics.source-schema:shopping_service}") String sourceSchema
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.sourceSchema = normalizeSourceSchema(sourceSchema);
	}

	public StockoutSummaryQueryResult findSummary(
		PeriodRange periodRange,
		Long storeId,
		Long categoryId
	) {
		String sql = """
			WITH target_inventories AS (
				SELECT
					i.inventory_id,
					i.product_id,
					i.store_id,
					i.stock_quantity,
					i.updated_at
				FROM %s i
				JOIN %s p ON p.product_id = i.product_id
				WHERE p.deleted_at IS NULL
					AND (:storeId IS NULL OR i.store_id = :storeId)
					AND (:categoryId IS NULL OR p.category_id = :categoryId)
			)
			SELECT
				COUNT(DISTINCT product_id) AS total_inventory_product_count,
				COUNT(DISTINCT CASE
					WHEN stock_quantity <= 0
						AND updated_at >= :fromAt
						AND updated_at < :toAt
					THEN product_id
				END) AS stockout_product_count,
				COUNT(CASE
					WHEN stock_quantity <= 0
						AND updated_at >= :fromAt
						AND updated_at < :toAt
					THEN 1
				END) AS stockout_occurrence_count,
				COUNT(DISTINCT CASE
					WHEN stock_quantity BETWEEN 1 AND 5
					THEN product_id
				END) AS low_stock_product_count
			FROM target_inventories
			""".formatted(
			table("inventories"),
			table("products")
		);

		Map<String, Object> params = params(periodRange, storeId, categoryId);

		return jdbcTemplate.queryForObject(
			sql,
			params,
			(rs, rowNum) -> new StockoutSummaryQueryResult(
				rs.getLong("total_inventory_product_count"),
				rs.getLong("stockout_product_count"),
				rs.getLong("stockout_occurrence_count"),
				rs.getLong("low_stock_product_count")
			)
		);
	}

	public List<StockoutDailyQueryResult> findDailyTrends(
		PeriodRange periodRange,
		Long storeId,
		Long categoryId
	) {
		String sql = """
			SELECT
				CAST(i.updated_at AS DATE) AS analysis_date,
				COUNT(*) AS stockout_occurrence_count,
				COUNT(DISTINCT i.product_id) AS stockout_product_count
			FROM %s i
			JOIN %s p ON p.product_id = i.product_id
			WHERE p.deleted_at IS NULL
				AND i.stock_quantity <= 0
				AND i.updated_at >= :fromAt
				AND i.updated_at < :toAt
				AND (:storeId IS NULL OR i.store_id = :storeId)
				AND (:categoryId IS NULL OR p.category_id = :categoryId)
			GROUP BY CAST(i.updated_at AS DATE)
			ORDER BY CAST(i.updated_at AS DATE) ASC
			""".formatted(
			table("inventories"),
			table("products")
		);

		Map<String, Object> params = params(periodRange, storeId, categoryId);

		return jdbcTemplate.query(
			sql,
			params,
			(rs, rowNum) -> new StockoutDailyQueryResult(
				toLocalDate(rs.getDate("analysis_date")),
				rs.getLong("stockout_occurrence_count"),
				rs.getLong("stockout_product_count")
			)
		);
	}

	public List<StockoutProductQueryResult> findStockoutProducts(
		PeriodRange periodRange,
		Long storeId,
		Long categoryId,
		int limit
	) {
		String sql = """
			SELECT
				p.product_id,
				p.product_name,
				p.brand_name,
				p.category_id,
				c.category_name,
				i.store_id,
				i.stock_quantity,
				CASE
					WHEN i.stock_quantity <= 0 THEN 'OUT_OF_STOCK'
					WHEN i.stock_quantity <= 5 THEN 'LOW_STOCK'
					ELSE 'IN_STOCK'
				END AS stock_status,
				i.updated_at AS stockout_occurred_at
			FROM %s i
			JOIN %s p ON p.product_id = i.product_id
			LEFT JOIN %s c ON c.category_id = p.category_id
			WHERE p.deleted_at IS NULL
				AND i.stock_quantity <= 0
				AND i.updated_at >= :fromAt
				AND i.updated_at < :toAt
				AND (:storeId IS NULL OR i.store_id = :storeId)
				AND (:categoryId IS NULL OR p.category_id = :categoryId)
			ORDER BY
				i.updated_at DESC,
				i.stock_quantity ASC,
				p.product_id ASC
			LIMIT :limit
			""".formatted(
			table("inventories"),
			table("products"),
			table("categories")
		);

		Map<String, Object> params = params(periodRange, storeId, categoryId);
		params.put("limit", limit);

		return jdbcTemplate.query(
			sql,
			params,
			(rs, rowNum) -> new StockoutProductQueryResult(
				rs.getLong("product_id"),
				rs.getString("product_name"),
				rs.getString("brand_name"),
				rs.getLong("category_id"),
				rs.getString("category_name"),
				rs.getLong("store_id"),
				rs.getInt("stock_quantity"),
				rs.getString("stock_status"),
				toLocalDateTime(rs.getTimestamp("stockout_occurred_at"))
			)
		);
	}

	private Map<String, Object> params(PeriodRange periodRange, Long storeId, Long categoryId) {
		Map<String, Object> params = new HashMap<>();
		params.put("fromAt", periodRange.fromAt());
		params.put("toAt", periodRange.toExclusiveAt());
		params.put("storeId", storeId);
		params.put("categoryId", categoryId);
		return params;
	}

	private LocalDate toLocalDate(java.sql.Date date) {
		return date == null ? null : date.toLocalDate();
	}

	private java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
		return timestamp == null ? null : timestamp.toLocalDateTime();
	}

	private String table(String tableName) {
		if (sourceSchema.isBlank()) {
			return tableName;
		}

		return sourceSchema + "." + tableName;
	}

	private String normalizeSourceSchema(String sourceSchema) {
		if (sourceSchema == null || sourceSchema.isBlank()) {
			return "";
		}

		if (!SAFE_SCHEMA_PATTERN.matcher(sourceSchema).matches()) {
			throw new IllegalArgumentException("올바르지 않은 스키마명입니다.");
		}

		return sourceSchema;
	}
}