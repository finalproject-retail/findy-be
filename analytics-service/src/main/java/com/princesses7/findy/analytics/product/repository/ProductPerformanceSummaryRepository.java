package com.princesses7.findy.analytics.product.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceProductQueryResult;
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceSummaryQueryResult;

@Repository
public class ProductPerformanceSummaryRepository {

	private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final String sourceSchema;

	public ProductPerformanceSummaryRepository(
		NamedParameterJdbcTemplate jdbcTemplate,
		@Value("${analytics.source-schema:shopping_service}") String sourceSchema
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.sourceSchema = normalizeSourceSchema(sourceSchema);
	}

	public ProductPerformanceSummaryQueryResult findSummary(PeriodRange periodRange, Long storeId) {
		String sql = """
			WITH target_products AS (
				SELECT p.product_id
				FROM %s p
				WHERE p.deleted_at IS NULL
					AND (:storeId IS NULL OR EXISTS (
						SELECT 1
						FROM %s i
						WHERE i.product_id = p.product_id
							AND i.store_id = :storeId
					))
			),
			view_summary AS (
				SELECT
					rvp.product_id,
					COUNT(rvp.recent_view_id) AS view_count
				FROM %s rvp
				JOIN target_products tp ON tp.product_id = rvp.product_id
				WHERE rvp.created_at >= :fromAt
					AND rvp.created_at < :toAt
				GROUP BY rvp.product_id
			),
			order_summary AS (
				SELECT
					oi.product_id,
					COUNT(DISTINCT o.order_id) AS order_count,
					COALESCE(SUM(oi.quantity), 0) AS order_quantity,
					COALESCE(SUM(oi.final_amount), 0) AS sales_amount
				FROM %s oi
				JOIN %s o ON o.order_id = oi.order_id
				JOIN target_products tp ON tp.product_id = oi.product_id
				WHERE o.created_at >= :fromAt
					AND o.created_at < :toAt
					AND o.order_status = 'COMPLETED'
				GROUP BY oi.product_id
			),
			order_scope AS (
				SELECT DISTINCT o.order_id
				FROM %s o
				JOIN %s oi ON oi.order_id = o.order_id
				JOIN target_products tp ON tp.product_id = oi.product_id
				WHERE o.created_at >= :fromAt
					AND o.created_at < :toAt
					AND o.order_status = 'COMPLETED'
			)
			SELECT
				(SELECT COUNT(*) FROM target_products) AS total_product_count,
				COALESCE((SELECT COUNT(*) FROM view_summary WHERE view_count > 0), 0) AS viewed_product_count,
				COALESCE((SELECT COUNT(*) FROM order_summary WHERE order_quantity > 0), 0) AS ordered_product_count,
				COALESCE((SELECT SUM(view_count) FROM view_summary), 0) AS total_view_count,
				COALESCE((SELECT COUNT(*) FROM order_scope), 0) AS total_order_count,
				COALESCE((SELECT SUM(order_quantity) FROM order_summary), 0) AS total_order_quantity,
				COALESCE((SELECT SUM(sales_amount) FROM order_summary), 0) AS total_sales_amount
			""".formatted(
			table("products"),
			table("inventories"),
			table("recent_view_products"),
			table("order_items"),
			table("orders"),
			table("orders"),
			table("order_items")
		);

		Map<String, Object> params = new HashMap<>();
		params.put("storeId", storeId);
		params.put("fromAt", periodRange.fromAt());
		params.put("toAt", periodRange.toExclusiveAt());

		return jdbcTemplate.queryForObject(
			sql,
			params,
			(rs, rowNum) -> new ProductPerformanceSummaryQueryResult(
				rs.getLong("total_product_count"),
				rs.getLong("viewed_product_count"),
				rs.getLong("ordered_product_count"),
				rs.getLong("total_view_count"),
				rs.getLong("total_order_count"),
				rs.getLong("total_order_quantity"),
				rs.getLong("total_sales_amount")
			)
		);
	}

	public List<ProductPerformanceProductQueryResult> findTopProducts(
		PeriodRange periodRange,
		Long storeId,
		int limit
	) {
		String sql = """
			WITH target_products AS (
				SELECT p.product_id
				FROM %s p
				WHERE p.deleted_at IS NULL
					AND (:storeId IS NULL OR EXISTS (
						SELECT 1
						FROM %s i
						WHERE i.product_id = p.product_id
							AND i.store_id = :storeId
					))
			),
			view_summary AS (
				SELECT
					rvp.product_id,
					COUNT(rvp.recent_view_id) AS view_count
				FROM %s rvp
				JOIN target_products tp ON tp.product_id = rvp.product_id
				WHERE rvp.created_at >= :fromAt
					AND rvp.created_at < :toAt
				GROUP BY rvp.product_id
			),
			order_summary AS (
				SELECT
					oi.product_id,
					COUNT(DISTINCT o.order_id) AS order_count,
					COALESCE(SUM(oi.quantity), 0) AS order_quantity,
					COALESCE(SUM(oi.final_amount), 0) AS sales_amount
				FROM %s oi
				JOIN %s o ON o.order_id = oi.order_id
				JOIN target_products tp ON tp.product_id = oi.product_id
				WHERE o.created_at >= :fromAt
					AND o.created_at < :toAt
					AND o.order_status = 'COMPLETED'
				GROUP BY oi.product_id
			)
			SELECT
				p.product_id,
				p.product_name,
				p.brand_name,
				p.category_id,
				c.category_name,
				COALESCE(vs.view_count, 0) AS view_count,
				COALESCE(os.order_count, 0) AS order_count,
				COALESCE(os.order_quantity, 0) AS order_quantity,
				COALESCE(os.sales_amount, 0) AS sales_amount
			FROM target_products tp
			JOIN %s p ON p.product_id = tp.product_id
			LEFT JOIN %s c ON c.category_id = p.category_id
			LEFT JOIN view_summary vs ON vs.product_id = p.product_id
			LEFT JOIN order_summary os ON os.product_id = p.product_id
			WHERE COALESCE(vs.view_count, 0) > 0
				OR COALESCE(os.order_quantity, 0) > 0
			ORDER BY
				COALESCE(os.sales_amount, 0) DESC,
				COALESCE(os.order_quantity, 0) DESC,
				COALESCE(vs.view_count, 0) DESC,
				p.product_id ASC
			LIMIT :limit
			""".formatted(
			table("products"),
			table("inventories"),
			table("recent_view_products"),
			table("order_items"),
			table("orders"),
			table("products"),
			table("categories")
		);

		Map<String, Object> params = new HashMap<>();
		params.put("storeId", storeId);
		params.put("fromAt", periodRange.fromAt());
		params.put("toAt", periodRange.toExclusiveAt());
		params.put("limit", limit);

		return jdbcTemplate.query(
			sql,
			params,
			(rs, rowNum) -> new ProductPerformanceProductQueryResult(
				rs.getLong("product_id"),
				rs.getString("product_name"),
				rs.getString("brand_name"),
				rs.getLong("category_id"),
				rs.getString("category_name"),
				rs.getLong("view_count"),
				rs.getLong("order_count"),
				rs.getLong("order_quantity"),
				rs.getLong("sales_amount")
			)
		);
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