package com.princesses7.findy.recommendation.chatbot.repository;

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
		String normalizedKeyword = keyword == null ? "" : keyword.trim();

		if (normalizedKeyword.isBlank()) {
			return List.of();
		}

		String sql = """
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
			    p.grid_id,
			    i.stock_quantity,
			    CASE
			        WHEN i.stock_quantity IS NULL THEN NULL
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
			    p.original_price ASC,
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
			(rs, rowNum) -> new ChatbotShoppingProduct(
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
			)
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

		String sql = """
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
			    p.grid_id,
			    i.stock_quantity,
			    CASE
			        WHEN i.stock_quantity IS NULL THEN NULL
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
			(rs, rowNum) -> new ChatbotShoppingProduct(
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
			)
		);
	}
}