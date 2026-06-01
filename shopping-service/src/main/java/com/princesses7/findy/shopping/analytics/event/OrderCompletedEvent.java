package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;
import java.util.List;

public record OrderCompletedEvent(
	String eventId,
	ShoppingEventType eventType,
	Instant occurredAt,
	Long userId,
	Long storeId,
	Long orderId,
	Long shoppingListId,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	List<OrderLineItemPayload> orderItems
) implements ShoppingAnalyticsEvent {

	public OrderCompletedEvent(
		Long userId,
		Long storeId,
		Long orderId,
		Long shoppingListId,
		int totalAmount,
		int discountAmount,
		int finalAmount,
		List<OrderLineItemPayload> orderItems
	) {
		this(
			ShoppingEventIds.newEventId(),
			ShoppingEventType.ORDER_COMPLETED,
			ShoppingEventIds.now(),
			userId,
			storeId,
			orderId,
			shoppingListId,
			totalAmount,
			discountAmount,
			finalAmount,
			orderItems
		);
	}
}
