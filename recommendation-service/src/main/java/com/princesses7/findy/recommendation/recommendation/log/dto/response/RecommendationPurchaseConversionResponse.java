package com.princesses7.findy.recommendation.recommendation.log.dto.response;

public record RecommendationPurchaseConversionResponse(
	Long userId,
	Long orderId,
	int convertedCount
) {
}