package com.princesses7.findy.shopping.analytics.event;

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
