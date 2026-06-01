package com.princesses7.findy.shopping.analytics.publisher;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.analytics.config.ShoppingKafkaProperties;
import com.princesses7.findy.shopping.analytics.event.CartItemAddedEvent;
import com.princesses7.findy.shopping.analytics.event.OrderCompletedEvent;
import com.princesses7.findy.shopping.analytics.event.OrderLineItemPayload;
import com.princesses7.findy.shopping.analytics.event.ProductViewSource;
import com.princesses7.findy.shopping.analytics.event.ProductViewedEvent;
import com.princesses7.findy.shopping.analytics.event.RecommendationSource;
import com.princesses7.findy.shopping.analytics.event.ShoppingListItemAddedEvent;
import com.princesses7.findy.shopping.analytics.support.ProductAnalyticsMetadata;
import com.princesses7.findy.shopping.analytics.support.ProductAnalyticsMetadataResolver;
import com.princesses7.findy.shopping.order.entity.Order;
import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShoppingAnalyticsEventService {

	private final ShoppingKafkaProperties shoppingKafkaProperties;
	private final ShoppingAnalyticsEventPublisher shoppingAnalyticsEventPublisher;
	private final ProductAnalyticsMetadataResolver productAnalyticsMetadataResolver;
	private final PromotionProductRepository promotionProductRepository;

	public void publishProductViewed(Long userId, Long productId) {
		publishProductViewed(userId, productId, ProductViewSource.DIRECT, null, null);
	}

	/**
	 * 지도 행사 핀 탭 → 상세 진입 시 호출 (API 레이어에서 연동).
	 */
	public void publishMapPromotionProductViewed(
		Long userId,
		Long productId,
		Long promotionId,
		Long pinGridId
	) {
		publishProductViewed(userId, productId, ProductViewSource.MAP_PROMOTION, promotionId, pinGridId);
	}

	private void publishProductViewed(
		Long userId,
		Long productId,
		ProductViewSource viewSource,
		Long promotionId,
		Long pinGridId
	) {
		ProductAnalyticsMetadata metadata = productAnalyticsMetadataResolver.resolve(productId);
		ProductViewSource resolvedViewSource = viewSource == null ? ProductViewSource.DIRECT : viewSource;
		Long resolvedPromotionId = resolvePromotionId(productId, promotionId, resolvedViewSource);
		Long eventGridId = resolveGridId(metadata.gridId(), pinGridId, resolvedViewSource);

		shoppingAnalyticsEventPublisher.publish(
			new ProductViewedEvent(
				userId,
				storeId(),
				metadata.productId(),
				metadata.categoryId(),
				eventGridId,
				resolvedViewSource,
				resolvedPromotionId
			)
		);
	}

	public void publishCartItemAdded(
		Long userId,
		Long productId,
		int quantity,
		RecommendationSource recommendationSource,
		Long originalProductId
	) {
		ProductAnalyticsMetadata metadata = productAnalyticsMetadataResolver.resolve(productId);
		shoppingAnalyticsEventPublisher.publish(
			new CartItemAddedEvent(
				userId,
				storeId(),
				metadata.productId(),
				metadata.categoryId(),
				metadata.gridId(),
				quantity,
				recommendationSource,
				originalProductId
			)
		);
	}

	public void publishShoppingListItemAdded(
		Long userId,
		List<Long> productIds,
		RecommendationSource recommendationSource,
		Long originalProductId
	) {
		if (productIds == null || productIds.isEmpty()) {
			return;
		}

		shoppingAnalyticsEventPublisher.publish(
			new ShoppingListItemAddedEvent(
				userId,
				storeId(),
				productIds,
				recommendationSource,
				originalProductId
			)
		);
	}

	public void publishShoppingListItemsFromCart(
		Long userId,
		ShoppingList shoppingList,
		RecommendationSource recommendationSource
	) {
		List<Long> productIds = shoppingList.getShoppingListItems().stream()
			.map(item -> item.getProductId())
			.toList();

		publishShoppingListItemAdded(userId, productIds, recommendationSource, null);
	}

	public void publishOrderCompleted(Long userId, Order order) {
		List<OrderLineItemPayload> orderItems = order.getOrderItems().stream()
			.map(this::toOrderLineItemPayload)
			.toList();

		shoppingAnalyticsEventPublisher.publish(
			new OrderCompletedEvent(
				userId,
				storeId(),
				order.getOrderId(),
				order.getShoppingListId(),
				order.getTotalAmount(),
				order.getDiscountAmount(),
				order.getFinalAmount(),
				orderItems
			)
		);
	}

	private Long resolvePromotionId(
		Long productId,
		Long promotionId,
		ProductViewSource viewSource
	) {
		if (promotionId != null) {
			return promotionId;
		}

		if (viewSource != ProductViewSource.MAP_PROMOTION) {
			return null;
		}

		return findActivePromotionProducts(productId).stream()
			.findFirst()
			.map(promotionProduct -> promotionProduct.getPromotion().getPromotionId())
			.orElse(null);
	}

	private Long resolveGridId(Long categoryGridId, Long pinGridId, ProductViewSource viewSource) {
		if (viewSource == ProductViewSource.MAP_PROMOTION && pinGridId != null) {
			return pinGridId;
		}

		return categoryGridId;
	}

	private List<PromotionProduct> findActivePromotionProducts(Long productId) {
		return promotionProductRepository.findApplicablePromotionProducts(
			productId,
			PromotionStatus.ENDED,
			LocalDateTime.now()
		);
	}

	private OrderLineItemPayload toOrderLineItemPayload(OrderItem orderItem) {
		ProductAnalyticsMetadata metadata = productAnalyticsMetadataResolver.resolve(
			orderItem.getProductId()
		);

		return new OrderLineItemPayload(
			metadata.productId(),
			metadata.categoryId(),
			metadata.gridId(),
			orderItem.getQuantity(),
			orderItem.getProductPrice(),
			orderItem.getDiscountAmount(),
			orderItem.getFinalAmount(),
			RecommendationSource.DIRECT,
			null
		);
	}

	private Long storeId() {
		return shoppingKafkaProperties.defaultStoreId();
	}
}
