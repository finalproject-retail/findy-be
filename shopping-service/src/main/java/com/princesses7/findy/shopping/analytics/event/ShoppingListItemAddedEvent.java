package com.princesses7.findy.shopping.analytics.event;

import java.time.Instant;
import java.util.List;

/**
 * 장바구니 체크 상품 → 쇼핑리스트 생성, 또는 검색으로 쇼핑리스트에 상품 추가 시 발행.
 * 체크된 상품은 {@code productIds} 목록으로 한 번에 전달한다.
 */
public record ShoppingListItemAddedEvent(
	String eventId,
	ShoppingEventType eventType,
	Instant occurredAt,
	Long userId,
	Long storeId,
	List<Long> productIds,
	RecommendationSource recommendationSource,
	Long originalProductId
) implements ShoppingAnalyticsEvent {

	public ShoppingListItemAddedEvent(
		Long userId,
		Long storeId,
		List<Long> productIds,
		RecommendationSource recommendationSource,
		Long originalProductId
	) {
		this(
			ShoppingEventIds.newEventId(),
			ShoppingEventType.SHOPPING_LIST_ITEM_ADDED,
			ShoppingEventIds.now(),
			userId,
			storeId,
			productIds == null ? List.of() : List.copyOf(productIds),
			recommendationSource == null ? RecommendationSource.DIRECT : recommendationSource,
			originalProductId
		);
	}
}
