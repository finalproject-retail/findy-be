package com.princesses7.findy.analytics.analytics.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.analytics.analytics.dto.query.ProductViewRankingQueryResult;

@Repository
public class ProductViewAnalyticsRepository {

	private static final Pattern SAFE_SCHEMA_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

	private final JdbcTemplate jdbcTemplate;
	private final String sourceSchema;

	public ProductViewAnalyticsRepository(
		JdbcTemplate jdbcTemplate,
		@Value("${analytics.source-schema:shopping_service}") String sourceSchema
	) {
		this.jdbcTemplate = jdbcTemplate;
		this.sourceSchema = normalizeSourceSchema(sourceSchema);
	}

	public Long countTotalViews(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		String sql = """
			SELECT COUNT(rvp.recent_view_id)
			FROM %s rvp
			JOIN %s p ON p.product_id = rvp.product_id
			WHERE rvp.created_at >= ?
			  AND rvp.created_at < ?
			  AND p.is_deleted = false
			""".formatted(
			table("recent_view_products"),
			table("products")
		);

		Number result = jdbcTemplate.queryForObject(
			sql,
			Number.class,
			startDateTime,
			endDateTime
		);

		return result == null ? 0L : result.longValue();
	}

	public Long countViewedProducts(LocalDateTime startDateTime, LocalDateTime endDateTime) {
		String sql = """
			SELECT COUNT(DISTINCT rvp.product_id)
			FROM %s rvp
			JOIN %s p ON p.product_id = rvp.product_id
			WHERE rvp.created_at >= ?
			  AND rvp.created_at < ?
			  AND p.is_deleted = false
			""".formatted(
			table("recent_view_products"),
			table("products")
		);

		Number result = jdbcTemplate.queryForObject(
			sql,
			Number.class,
			startDateTime,
			endDateTime
		);

		return result == null ? 0L : result.longValue();
	}

	public List<ProductViewRankingQueryResult> findProductViewRankings(
		LocalDateTime startDateTime,
		LocalDateTime endDateTime,
		int limit
	) {
		String sql = """
			SELECT
			    p.product_id,
			    p.product_name,
			    p.brand_name,
			    p.category_id,
			    COUNT(rvp.recent_view_id) AS view_count
			FROM %s rvp
			JOIN %s p ON p.product_id = rvp.product_id
			WHERE rvp.created_at >= ?
			  AND rvp.created_at < ?
			  AND p.is_deleted = false
			GROUP BY
			    p.product_id,
			    p.product_name,
			    p.brand_name,
			    p.category_id
			ORDER BY view_count DESC, p.product_id ASC
			LIMIT ?
			""".formatted(
			table("recent_view_products"),
			table("products")
		);

		return jdbcTemplate.query(
			sql,
			(rs, rowNum) -> new ProductViewRankingQueryResult(
				rs.getLong("product_id"),
				rs.getString("product_name"),
				rs.getString("brand_name"),
				rs.getLong("category_id"),
				rs.getLong("view_count")
			),
			startDateTime,
			endDateTime,
			limit
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