package com.princesses7.findy.recommendation.chatbot.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShoppingProductReadRepository {

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public List<ChatbotShoppingProduct> searchByKeyword(
		String keyword,
		Long storeId,
		int limit
	) {
		String normalizedKeyword = normalizeKeyword(keyword);

		if (normalizedKeyword.isBlank()) {
			return List.of();
		}

		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND (
					LOWER(p.product_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(p.brand_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(c.category_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				)
			ORDER BY
				CASE WHEN i.stock_quantity IS NULL THEN 1 ELSE 0 END,
				CASE WHEN i.stock_quantity <= 0 THEN 1 ELSE 0 END,
				p.product_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			Map.of(
				"keyword", normalizedKeyword,
				"storeId", storeId,
				"limit", limit
			),
			this::mapProduct
		);
	}

	public List<ChatbotShoppingProduct> searchIngredientCandidates(
		String ingredientName,
		Long storeId,
		int limit
	) {
		String normalizedKeyword = normalizeKeyword(ingredientName);

		if (normalizedKeyword.isBlank()) {
			return List.of();
		}

		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND i.stock_quantity > 0
				AND (
					LOWER(p.product_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(p.brand_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(COALESCE(c.category_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
				)
			ORDER BY
				CASE
					WHEN LOWER(p.product_name) = LOWER(:keyword) THEN 0
					WHEN LOWER(p.product_name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
					WHEN LOWER(p.product_name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 2
					WHEN LOWER(COALESCE(c.category_name, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3
					WHEN LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 4
					ELSE 5
				END,
				p.product_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			Map.of(
				"keyword", normalizedKeyword,
				"storeId", storeId,
				"limit", limit
			),
			this::mapProduct
		);
	}

	public List<ChatbotShoppingProduct> findByProductIds(
		Collection<Long> productIds,
		Long storeId,
		int limit
	) {
		if (productIds == null || productIds.isEmpty()) {
			return List.of();
		}

		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND p.product_id IN (:productIds)
			ORDER BY
				CASE WHEN i.stock_quantity IS NULL THEN 1 ELSE 0 END,
				CASE WHEN i.stock_quantity <= 0 THEN 1 ELSE 0 END,
				p.product_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			Map.of(
				"productIds", productIds,
				"storeId", storeId,
				"limit", limit
			),
			this::mapProduct
		);
	}

	private String baseSelectSql() {
		return """
			SELECT
				p.product_id,
				p.category_id,
				COALESCE(c.category_name, '') AS category_name,
				p.brand_name,
				p.product_name,
				p.image_url,
				p.original_price,
				p.sales_unit,
				p.volume,
				p.allergy_info,
				p.badge_text,
				p.sale_status,
				pl.access_grid_id AS grid_id,
				i.stock_quantity,
				CASE
					WHEN i.stock_quantity IS NULL THEN 'UNKNOWN'
					WHEN i.stock_quantity <= 0 THEN 'OUT_OF_STOCK'
					WHEN i.stock_quantity <= 5 THEN 'LOW_STOCK'
					ELSE 'IN_STOCK'
				END AS stock_status
			FROM shopping_service.products p
			LEFT JOIN shopping_service.categories c
				ON c.category_id = p.category_id
			LEFT JOIN shopping_service.inventories i
				ON i.product_id = p.product_id
				AND i.store_id = :storeId
			LEFT JOIN shopping_service.product_locations pl
				ON pl.product_id = p.product_id
				AND pl.store_id = :storeId
			""";
	}

	private ChatbotShoppingProduct mapProduct(
		ResultSet rs,
		int rowNum
	) throws SQLException {
		return new ChatbotShoppingProduct(
			rs.getLong("product_id"),
			rs.getLong("category_id"),
			rs.getString("category_name"),
			rs.getString("brand_name"),
			rs.getString("product_name"),
			rs.getString("image_url"),
			(Integer)rs.getObject("original_price"),
			rs.getString("sales_unit"),
			rs.getString("volume"),
			rs.getString("allergy_info"),
			rs.getString("badge_text"),
			rs.getString("sale_status"),
			(Long)rs.getObject("grid_id"),
			(Integer)rs.getObject("stock_quantity"),
			rs.getString("stock_status")
		);
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}

		return keyword.trim();
	}
}