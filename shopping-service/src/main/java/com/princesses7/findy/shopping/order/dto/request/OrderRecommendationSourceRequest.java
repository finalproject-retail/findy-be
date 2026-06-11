package com.princesses7.findy.shopping.order.dto.request;

public record OrderRecommendationSourceRequest(
	Long productId,
	Long recommendationLogId,
	String recommendationType,
	Long sourceProductId,
	String displayLocation
) {
}