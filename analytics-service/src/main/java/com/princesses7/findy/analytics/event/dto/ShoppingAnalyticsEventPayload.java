package com.princesses7.findy.analytics.event.dto;

import java.time.Instant;
import java.util.List;

public record ShoppingAnalyticsEventPayload(
	String eventId,
	ShoppingAnalyticsEventType eventType,
	Instant occurredAt,
	Long userId,
	Long storeId,
	Long productId,
	Long categoryId,
	Long gridId,
	Integer quantity,
	RecommendationSource recommendationSource,
	Long originalProductId,
	ProductViewSource viewSource,
	Long promotionId,
	Long orderId,
	Long shoppingListId,
	Integer totalAmount,
	Integer discountAmount,
	Integer finalAmount,
	List<Long> productIds,
	List<OrderLineItemPayload> orderItems
) {
}
