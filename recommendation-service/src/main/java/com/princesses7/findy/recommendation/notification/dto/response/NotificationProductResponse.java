package com.princesses7.findy.recommendation.notification.dto.response;

import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;

public record NotificationProductResponse(
	Long productId,
	String productName,
	String brandName,
	String imageUrl,
	Integer originalPrice
) {

	public static NotificationProductResponse from(ProductRecommendationResponse recommendation) {
		return new NotificationProductResponse(
			recommendation.productId(),
			recommendation.productName(),
			recommendation.brandName(),
			recommendation.imageUrl(),
			recommendation.originalPrice()
		);
	}

	public static NotificationProductResponse from(PromotionProductRecommendationResponse recommendation) {
		return new NotificationProductResponse(
			recommendation.productId(),
			recommendation.productName(),
			recommendation.brandName(),
			recommendation.imageUrl(),
			recommendation.originalPrice()
		);
	}
}