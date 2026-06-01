package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY,
	property = "eventType",
	visible = true
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = ProductViewedEvent.class, name = "PRODUCT_VIEWED"),
	@JsonSubTypes.Type(value = CartItemAddedEvent.class, name = "CART_ITEM_ADDED"),
	@JsonSubTypes.Type(value = ShoppingListItemAddedEvent.class, name = "SHOPPING_LIST_ITEM_ADDED"),
	@JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "ORDER_COMPLETED")
})
public sealed interface ShoppingAnalyticsEvent permits
	ProductViewedEvent,
	CartItemAddedEvent,
	ShoppingListItemAddedEvent,
	OrderCompletedEvent {

	String eventId();

	ShoppingEventType eventType();

	Instant occurredAt();

	Long userId();

	Long storeId();
}
