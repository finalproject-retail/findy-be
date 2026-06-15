package com.princesses7.findy.analytics.event.dto;

public record OrderLineItemPayload(
	Long productId,
	Long categoryId,
	Long gridId,
	int quantity,
	int productPrice,
	int discountAmount,
	int finalAmount,
	RecommendationSource recommendationSource,
	Long originalProductId
) {
}
