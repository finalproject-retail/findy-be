package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record ProductRecommendationResponse(
	Long recommendationLogId,
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	Long promotionId,
	String promotionName,
	double score,
	RecommendationType recommendationType,
	String reason
) {

	public static ProductRecommendationResponse from(
		ProductSnapshot product,
		double score,
		RecommendationType recommendationType,
		String reason
	) {
		return from(
			product,
			null,
			score,
			recommendationType,
			reason
		);
	}

	public static ProductRecommendationResponse from(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		double score,
		RecommendationType recommendationType,
		String reason
	) {
		Integer originalPrice = product.getOriginalPrice();
		Integer salePrice = calculateSalePrice(originalPrice, promotionProduct);
		BigDecimal discountRate = calculateDiscountRate(originalPrice, salePrice);
		Long promotionId = promotionProduct == null || promotionProduct.getPromotion() == null
			? null
			: promotionProduct.getPromotion().getPromotionId();
		String promotionName = promotionProduct == null || promotionProduct.getPromotion() == null
			? null
			: promotionProduct.getPromotion().getPromotionName();

		return new ProductRecommendationResponse(
			null,
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			originalPrice,
			salePrice,
			discountRate,
			promotionId,
			promotionName,
			round(score),
			recommendationType,
			reason
		);
	}

	public ProductRecommendationResponse withRecommendationLogId(Long recommendationLogId) {
		return new ProductRecommendationResponse(
			recommendationLogId,
			productId,
			productName,
			brandName,
			imageUrl,
			originalPrice,
			salePrice,
			discountRate,
			promotionId,
			promotionName,
			score,
			recommendationType,
			reason
		);
	}

	private static Integer calculateSalePrice(
		Integer originalPrice,
		PromotionProductSnapshot promotionProduct
	) {
		if (originalPrice == null) {
			return 0;
		}

		if (promotionProduct == null || promotionProduct.getPromotionPrice() == null) {
			return calculateRateDiscountSalePrice(originalPrice, promotionProduct);
		}

		Integer promotionPrice = promotionProduct.getPromotionPrice();

		if (promotionPrice <= 0 || promotionPrice >= originalPrice) {
			return originalPrice;
		}

		return promotionPrice;
	}

	private static Integer calculateRateDiscountSalePrice(
		Integer originalPrice,
		PromotionProductSnapshot promotionProduct
	) {
		if (promotionProduct == null || promotionProduct.getPromotion() == null) {
			return originalPrice;
		}

		BigDecimal promotionDiscountRate = promotionProduct.getPromotion().getDiscountRate();

		if (promotionDiscountRate == null || promotionDiscountRate.compareTo(BigDecimal.ZERO) <= 0) {
			return originalPrice;
		}

		if (promotionDiscountRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
			return originalPrice;
		}

		return BigDecimal.valueOf(originalPrice)
			.multiply(BigDecimal.valueOf(100).subtract(promotionDiscountRate))
			.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
			.intValue();
	}

	private static BigDecimal calculateDiscountRate(
		Integer originalPrice,
		Integer salePrice
	) {
		if (originalPrice == null || salePrice == null || originalPrice <= 0) {
			return BigDecimal.ZERO;
		}

		if (salePrice <= 0 || salePrice >= originalPrice) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(originalPrice - salePrice)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(originalPrice), 2, RoundingMode.HALF_UP);
	}

	private static double round(double score) {
		return BigDecimal.valueOf(score)
			.setScale(3, RoundingMode.HALF_UP)
			.doubleValue();
	}
}
