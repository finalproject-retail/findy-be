package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;

public record CartItemAddedEvent(
	String eventId,
	ShoppingEventType eventType,
	Instant occurredAt,
	Long userId,
	Long storeId,
	Long productId,
	Long categoryId,
	Long gridId,
	int quantity,
	RecommendationSource recommendationSource,
	Long originalProductId
) implements ShoppingAnalyticsEvent {

	public CartItemAddedEvent(
		Long userId,
		Long storeId,
		Long productId,
		Long categoryId,
		Long gridId,
		int quantity,
		RecommendationSource recommendationSource,
		Long originalProductId
	) {
		this(
			ShoppingEventIds.newEventId(),
			ShoppingEventType.CART_ITEM_ADDED,
			ShoppingEventIds.now(),
			userId,
			storeId,
			productId,
			categoryId,
			gridId,
			quantity,
			recommendationSource,
			originalProductId
		);
	}
}
