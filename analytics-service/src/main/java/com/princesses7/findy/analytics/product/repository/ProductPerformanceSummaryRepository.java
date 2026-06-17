package com.princesses7.findy.analytics.product.repository;

import java.sql.Types;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
	private final String userSourceSchema;

	public ProductPerformanceSummaryRepository(
		NamedParameterJdbcTemplate jdbcTemplate,
		@Value("${analytics.source-schema:shopping_service}") String sourceSchema,
		@Value("${analytics.user-source-schema:user_service}") String userSourceSchema
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.sourceSchema = normalizeSourceSchema(sourceSchema);
		this.userSourceSchema = normalizeSourceSchema(userSourceSchema);
	}

	public ProductPerformanceSummaryQueryResult findSummary(
		PeriodRange periodRange,
		Long storeId,
		List<Long> categoryIds
	) {
		String sql = """
			WITH target_products AS (
				SELECT p.product_id
				FROM %s p
				WHERE p.deleted_at IS NULL
					AND (:categoryIdsEmpty = TRUE OR p.category_id IN (:categoryIds))
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
				WHERE rvp.viewed_at >= :fromAt
					AND rvp.viewed_at < :toAt
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
			userTable("recent_view_products"),
			table("order_items"),
			table("orders"),
			table("orders"),
			table("order_items")
		);

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("categoryIdsEmpty", isCategoryIdsEmpty(categoryIds), Types.BOOLEAN)
			.addValue("categoryIds", getSafeCategoryIds(categoryIds))
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP);

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
		List<Long> categoryIds,
		int limit
	) {
		String sql = """
			WITH target_products AS (
				SELECT p.product_id
				FROM %s p
				WHERE p.deleted_at IS NULL
					AND (:categoryIdsEmpty = TRUE OR p.category_id IN (:categoryIds))
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
				WHERE rvp.viewed_at >= :fromAt
					AND rvp.viewed_at < :toAt
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
			userTable("recent_view_products"),
			table("order_items"),
			table("orders"),
			table("products"),
			table("categories")
		);

		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("storeId", storeId, Types.BIGINT)
			.addValue("categoryIdsEmpty", isCategoryIdsEmpty(categoryIds), Types.BOOLEAN)
			.addValue("categoryIds", getSafeCategoryIds(categoryIds))
			.addValue("fromAt", periodRange.fromAt(), Types.TIMESTAMP)
			.addValue("toAt", periodRange.toExclusiveAt(), Types.TIMESTAMP)
			.addValue("limit", limit, Types.INTEGER);

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

	private boolean isCategoryIdsEmpty(List<Long> categoryIds) {
		return categoryIds == null || categoryIds.isEmpty();
	}

	private List<Long> getSafeCategoryIds(List<Long> categoryIds) {
		if (categoryIds == null || categoryIds.isEmpty()) {
			return List.of(-1L);
		}

		return categoryIds.stream()
			.filter(categoryId -> categoryId != null && categoryId > 0)
			.distinct()
			.toList();
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

	private String userTable(String tableName) {
		if (userSourceSchema.isBlank()) {
			return tableName;
		}

		return userSourceSchema + "." + tableName;
	}
}