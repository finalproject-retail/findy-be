package com.princesses7.findy.shopping.analytics.publisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
import com.princesses7.findy.shopping.order.dto.request.OrderRecommendationSourceRequest;
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
		publishOrderCompleted(userId, order, List.of());
	}

	public void publishOrderCompleted(
		Long userId,
		Order order,
		List<OrderRecommendationSourceRequest> recommendationSources
	) {
		Map<Long, OrderRecommendationSourceRequest> recommendationSourceMap = toRecommendationSourceMap(
			recommendationSources
		);

		List<OrderLineItemPayload> orderItems = order.getOrderItems().stream()
			.map(orderItem -> toOrderLineItemPayload(orderItem, recommendationSourceMap))
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

	private OrderLineItemPayload toOrderLineItemPayload(
		OrderItem orderItem,
		Map<Long, OrderRecommendationSourceRequest> recommendationSourceMap
	) {
		ProductAnalyticsMetadata metadata = productAnalyticsMetadataResolver.resolve(
			orderItem.getProductId()
		);
		OrderRecommendationSourceRequest recommendationSource = recommendationSourceMap.get(orderItem.getProductId());

		return new OrderLineItemPayload(
			metadata.productId(),
			metadata.categoryId(),
			metadata.gridId(),
			orderItem.getQuantity(),
			orderItem.getProductPrice(),
			orderItem.getDiscountAmount(),
			orderItem.getFinalAmount(),
			toRecommendationSource(recommendationSource),
			recommendationSource == null ? null : recommendationSource.sourceProductId()
		);
	}

	private Map<Long, OrderRecommendationSourceRequest> toRecommendationSourceMap(
		List<OrderRecommendationSourceRequest> recommendationSources
	) {
		if (recommendationSources == null || recommendationSources.isEmpty()) {
			return Map.of();
		}

		return recommendationSources.stream()
			.filter(Objects::nonNull)
			.filter(source -> source.productId() != null)
			.collect(Collectors.toMap(
				OrderRecommendationSourceRequest::productId,
				source -> source,
				(left, right) -> left
			));
	}

	private RecommendationSource toRecommendationSource(OrderRecommendationSourceRequest source) {
		if (source == null) {
			return RecommendationSource.DIRECT;
		}

		String recommendationType = source.recommendationType();
		if (recommendationType == null || recommendationType.isBlank()) {
			return RecommendationSource.RECOMMENDATION;
		}

		String normalizedType = recommendationType.trim().toUpperCase(Locale.ROOT);
		if (normalizedType.contains("SUBSTITUTE")) {
			return RecommendationSource.SUBSTITUTE;
		}
		if (normalizedType.contains("PROMOTION")) {
			return RecommendationSource.PROMOTION;
		}

		return RecommendationSource.RECOMMENDATION;
	}

	private Long storeId() {
		return shoppingKafkaProperties.defaultStoreId();
	}
}