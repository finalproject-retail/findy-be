package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public record ChatbotRecipeProductRecommendationResponse(
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Long categoryId,
	String categoryName,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	Integer stockQuantity,
	String stockStatus,
	String stockText,
	boolean selected,
	String substituteEndpoint
) {

	public static ChatbotRecipeProductRecommendationResponse from(
		Long userId,
		Long storeId,
		ProductSnapshot product,
		String categoryName,
		InventorySnapshot inventory,
		boolean selected
	) {
		return new ChatbotRecipeProductRecommendationResponse(
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			product.getCategoryId(),
			categoryName,
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus(),
			createStockText(inventory),
			selected,
			createSubstituteEndpoint(userId, storeId, product.getProductId())
		);
	}

	private static String createSubstituteEndpoint(
		Long userId,
		Long storeId,
		Long productId
	) {
		return "/api/v1/recommendations/products/%d/substitutes?userId=%d&storeId=%d&size=5&force=true"
			.formatted(productId, userId, storeId);
	}

	private static String createStockText(InventorySnapshot inventory) {
		if (inventory == null) {
			return "재고 정보 없음";
		}

		if (inventory.isOutOfStock()) {
			return "품절";
		}

		if (inventory.isLowStock() || inventory.getStockQuantity() <= 5) {
			return "품절임박 " + inventory.getStockQuantity() + "개";
		}

		return "재고 " + inventory.getStockQuantity() + "개";
	}
}