package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public record ProductRecommendationResponse(
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	double score,
	String recommendationType,
	String reason
) {

	public static ProductRecommendationResponse from(
		ProductSnapshot product,
		double score,
		String reason
	) {
		return new ProductRecommendationResponse(
			product.getProductId(),
			product.getProductName(),
			product.getBrandName(),
			product.getImageUrl(),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			Math.round(score * 10000.0) / 10000.0,
			"PERSONALIZED",
			reason
		);
	}
}