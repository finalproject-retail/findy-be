package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;

public record ProductViewedEvent(
	String eventId,
	ShoppingEventType eventType,
	Instant occurredAt,
	Long userId,
	Long storeId,
	Long productId,
	Long categoryId,
	Long gridId,
	ProductViewSource viewSource,
	Long promotionId
) implements ShoppingAnalyticsEvent {

	public ProductViewedEvent(
		Long userId,
		Long storeId,
		Long productId,
		Long categoryId,
		Long gridId,
		ProductViewSource viewSource,
		Long promotionId
	) {
		this(
			ShoppingEventIds.newEventId(),
			ShoppingEventType.PRODUCT_VIEWED,
			ShoppingEventIds.now(),
			userId,
			storeId,
			productId,
			categoryId,
			gridId,
			viewSource == null ? ProductViewSource.DIRECT : viewSource,
			promotionId
		);
	}
}
