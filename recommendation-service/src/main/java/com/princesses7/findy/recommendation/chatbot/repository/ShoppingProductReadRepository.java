package com.princesses7.findy.recommendation.chatbot.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

	public List<ChatbotShoppingProduct> findAiRecommendationCandidates(
		Long storeId,
		Collection<String> keywords,
		int limit
	) {
		List<String> normalizedKeywords = normalizeKeywords(keywords);

		if (normalizedKeywords.isEmpty()) {
			return findRecommendableCandidates(storeId, limit);
		}

		List<ChatbotShoppingProduct> keywordCandidates = searchByAnyKeyword(
			normalizedKeywords,
			storeId,
			limit
		);

		Map<Long, ChatbotShoppingProduct> productMap = new LinkedHashMap<>();

		for (ChatbotShoppingProduct product : keywordCandidates) {
			if (product != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.productId(), product);
			}
		}

		if (productMap.size() < limit) {
			for (ChatbotShoppingProduct product : findRecommendableCandidates(storeId, limit)) {
				if (product != null && product.isRecommendable()) {
					productMap.putIfAbsent(product.productId(), product);
				}

				if (productMap.size() >= limit) {
					break;
				}
			}
		}

		return productMap.values()
			.stream()
			.limit(limit)
			.toList();
	}

	public List<ChatbotShoppingProduct> findRecommendableCandidates(
		Long storeId,
		int limit
	) {
		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND COALESCE(i.stock_quantity, 0) > 0
			ORDER BY
				p.product_id DESC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			Map.of(
				"storeId", storeId,
				"limit", limit
			),
			this::mapProduct
		);
	}

	public List<ChatbotShoppingProduct> findIngredientJudgeCandidates(
		Long storeId,
		int limit
	) {
		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND COALESCE(i.stock_quantity, 0) > 0
			ORDER BY
				COALESCE(c.category_name, '') ASC,
				p.product_id ASC
			LIMIT :limit
			""";

		return jdbcTemplate.query(
			sql,
			Map.of(
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

	private List<ChatbotShoppingProduct> searchByAnyKeyword(
		List<String> keywords,
		Long storeId,
		int limit
	) {
		MapSqlParameterSource parameters = new MapSqlParameterSource()
			.addValue("storeId", storeId)
			.addValue("limit", limit);

		String keywordCondition = buildKeywordCondition(keywords, parameters);

		String sql = baseSelectSql() + """
			WHERE p.deleted_at IS NULL
				AND p.sale_status = 'ON_SALE'
				AND COALESCE(i.stock_quantity, 0) > 0
				AND (
					%s
				)
			ORDER BY
				CASE WHEN i.stock_quantity IS NULL THEN 1 ELSE 0 END,
				CASE WHEN i.stock_quantity <= 0 THEN 1 ELSE 0 END,
				p.product_id ASC
			LIMIT :limit
			""".formatted(keywordCondition);

		return jdbcTemplate.query(
			sql,
			parameters,
			this::mapProduct
		);
	}

	private String buildKeywordCondition(
		List<String> keywords,
		MapSqlParameterSource parameters
	) {
		List<String> conditions = new ArrayList<>();

		for (int index = 0; index < keywords.size(); index++) {
			String parameterName = "keyword" + index;
			parameters.addValue(parameterName, keywords.get(index));

			conditions.add("""
				LOWER(p.product_name) LIKE LOWER(CONCAT('%%', :%s, '%%'))
				OR LOWER(COALESCE(p.brand_name, '')) LIKE LOWER(CONCAT('%%', :%s, '%%'))
				OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%%', :%s, '%%'))
				OR LOWER(COALESCE(c.category_name, '')) LIKE LOWER(CONCAT('%%', :%s, '%%'))
				""".formatted(
				parameterName,
				parameterName,
				parameterName,
				parameterName
			));
		}

		return conditions.stream()
			.map(condition -> "(" + condition + ")")
			.collect(java.util.stream.Collectors.joining(" OR "));
	}

	private String baseSelectSql() {
		return """
			SELECT
				p.product_id,
				p.category_id,
				COALESCE(c.category_name, '') AS category_name,
				p.brand_name,
				p.product_name,
				COALESCE(p.description, '') AS description,
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
			rs.getString("description"),
			rs.getString("image_url"),
			(Integer)rs.getObject("original_price"),
			rs.getString("sales_unit"),
			rs.getString("volume"),
			rs.getString("allergy_info"),
			rs.getString("badge_text"),
			rs.getString("sale_status"),
			toLong(rs.getObject("grid_id")),
			(Integer)rs.getObject("stock_quantity"),
			rs.getString("stock_status")
		);
	}

	private List<String> normalizeKeywords(Collection<String> keywords) {
		if (keywords == null || keywords.isEmpty()) {
			return List.of();
		}

		return keywords.stream()
			.filter(Objects::nonNull)
			.flatMap(keyword -> Arrays.stream(keyword.split("[,\\s]+")))
			.map(this::normalizeKeyword)
			.filter(this::isUsefulKeyword)
			.distinct()
			.limit(8)
			.toList();
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}

		return keyword.trim()
			.replace("추천해주세요", "")
			.replace("추천해줘", "")
			.replace("추천", "")
			.replace("해주세요", "")
			.replace("해줘", "")
			.replaceAll("[^0-9A-Za-z가-힣]", "")
			.trim();
	}

	private boolean isUsefulKeyword(String keyword) {
		if (keyword == null || keyword.length() < 2) {
			return false;
		}

		return !List.of(
			"같은",
			"비슷한",
			"상품",
			"제품",
			"아무거나",
			"관련",
			"종류"
		).contains(keyword);
	}

	private Long toLong(Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Number number) {
			return number.longValue();
		}

		return Long.valueOf(value.toString());
	}
}
