package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record PromotionProductRecommendationResponse(
	Long recommendationLogId,
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Long categoryId,
	String categoryName,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	Long promotionId,
	Long promotionProductId,
	String promotionName,
	PromotionType promotionType,
	String benefitText,
	Integer promotionPrice,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	double score,
	RecommendationType recommendationType,
	String reason
) {

	public static PromotionProductRecommendationResponse of(
		ProductSnapshot product,
		String categoryName,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		double score,
		String reason
	) {
		return of(
			product,
			categoryName,
			promotionProduct,
			inventory,
			score,
			RecommendationType.PROMOTION,
			reason
		);
	}

	public static PromotionProductRecommendationResponse of(
		ProductSnapshot product,
		String categoryName,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		double score,
		RecommendationType recommendationType,
		String reason
	) {
		PromotionSnapshot promotion = promotionProduct.getPromotion();

		return new PromotionProductRecommendationResponse(
			null,
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			product.getCategoryId(),
			categoryName,
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			promotion.getPromotionId(),
			promotionProduct.getPromotionProductId(),
			promotion.getPromotionName(),
			promotion.getPromotionType(),
			promotion.getBenefitText(),
			promotionProduct.getPromotionPrice(),
			promotionProduct.getGridId(),
			inventory.getStockQuantity(),
			inventory.getStockStatus(),
			round(score),
			recommendationType,
			reason
		);
	}

	public PromotionProductRecommendationResponse withRecommendationLogId(Long recommendationLogId) {
		return new PromotionProductRecommendationResponse(
			recommendationLogId,
			productId,
			productName,
			brandName,
			imageUrl,
			categoryId,
			categoryName,
			originalPrice,
			salePrice,
			discountRate,
			promotionId,
			promotionProductId,
			promotionName,
			promotionType,
			benefitText,
			promotionPrice,
			gridId,
			stockQuantity,
			stockStatus,
			score,
			recommendationType,
			reason
		);
	}

	private static double round(double score) {
		return BigDecimal.valueOf(score)
			.setScale(3, RoundingMode.HALF_UP)
			.doubleValue();
	}
}