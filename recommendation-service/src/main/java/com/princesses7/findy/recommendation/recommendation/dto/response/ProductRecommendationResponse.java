package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
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
		return new ProductRecommendationResponse(
			null,
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
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