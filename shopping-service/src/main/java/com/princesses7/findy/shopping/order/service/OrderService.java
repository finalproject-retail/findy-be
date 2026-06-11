package com.princesses7.findy.shopping.order.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
import com.princesses7.findy.shopping.cart.service.CartCleanupService;
import com.princesses7.findy.shopping.coupon.dto.response.CouponDiscountResult;
import com.princesses7.findy.shopping.coupon.service.CouponService;
import com.princesses7.findy.shopping.order.dto.request.OrderRecommendationSourceRequest;
import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderDetailResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderItemResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderSummaryResponse;
import com.princesses7.findy.shopping.order.entity.Order;
import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.order.exception.OrderException;
import com.princesses7.findy.shopping.order.repository.OrderRepository;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountResponse;
import com.princesses7.findy.shopping.purchase.service.PurchaseAmountService;
import com.princesses7.findy.shopping.recommendation.client.RecommendationLogClient;
import com.princesses7.findy.shopping.shoppinglist.service.ShoppingListService;
import com.princesses7.findy.shopping.store.StoreIdSupport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final PurchaseAmountService purchaseAmountService;
	private final OrderRepository orderRepository;
	private final CartCleanupService cartCleanupService;
	private final CouponService couponService;
	private final ShoppingListService shoppingListService;
	private final ShoppingAnalyticsEventService shoppingAnalyticsEventService;
	private final RecommendationLogClient recommendationLogClient;

	@Transactional
	public OrderCreateResponse createOrder(
		Long userId,
		Long userCouponId,
		Integer usedReward,
		List<OrderRecommendationSourceRequest> recommendationSources,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		PurchaseAmountResponse amountResponse = purchaseAmountService.calculate(userId, resolvedStoreId);

		CouponDiscountResult couponDiscount = couponService.applyCoupon(
			userId,
			userCouponId,
			amountResponse.finalAmount()
		);

		int normalizedUsedReward = normalizeUsedReward(usedReward);
		int payableAmount = amountResponse.finalAmount() - couponDiscount.discountAmount();

		if (normalizedUsedReward > payableAmount) {
			throw new OrderException(INVALID_USED_REWARD);
		}

		int totalDiscountAmount = amountResponse.discountAmount() + couponDiscount.discountAmount();
		int finalAmount = payableAmount - normalizedUsedReward;

		Order order = Order.create(
			userId,
			amountResponse.shoppingListId(),
			couponDiscount.couponId(),
			amountResponse.totalAmount(),
			totalDiscountAmount,
			normalizedUsedReward,
			finalAmount
		);

		amountResponse.items().forEach(item ->
			order.addOrderItem(createOrderItem(item))
		);

		Order savedOrder = orderRepository.save(order);
		List<OrderRecommendationSourceRequest> normalizedRecommendationSources = normalizeRecommendationSources(
			recommendationSources,
			savedOrder
		);

		cartCleanupService.cleanupPurchasedCartItems(userId, savedOrder.getOrderItems());
		couponService.useCoupon(userId, userCouponId);
		shoppingListService.completeShopping(userId, resolvedStoreId);

		savedOrder.complete();

		shoppingAnalyticsEventService.publishOrderCompleted(
			userId,
			savedOrder,
			normalizedRecommendationSources
		);

		recommendationLogClient.sendPurchaseConversion(
			userId,
			savedOrder.getOrderId(),
			purchasedProductIds(savedOrder),
			normalizedRecommendationSources
		);

		return toResponse(savedOrder);
	}

	private OrderItem createOrderItem(PurchaseAmountItemResponse item) {
		return OrderItem.create(
			item.productId(),
			item.quantity(),
			item.productPrice(),
			item.discountAmount(),
			item.finalAmount()
		);
	}

	private OrderCreateResponse toResponse(Order order) {
		List<OrderItemResponse> items = order.getOrderItems().stream()
			.map(item -> new OrderItemResponse(
				item.getOrderItemId(),
				item.getProductId(),
				item.getQuantity(),
				item.getProductPrice(),
				item.getDiscountAmount(),
				item.getFinalAmount()
			))
			.toList();

		return new OrderCreateResponse(
			order.getOrderId(),
			order.getUserId(),
			order.getShoppingListId(),
			order.getCouponId(),
			order.getTotalAmount(),
			order.getDiscountAmount(),
			order.getUsedReward(),
			order.getFinalAmount(),
			order.getOrderStatus().name(),
			items
		);
	}

	private int normalizeUsedReward(Integer usedReward) {
		if (usedReward == null || usedReward <= 0) {
			return 0;
		}
		return usedReward;
	}

	private List<OrderRecommendationSourceRequest> normalizeRecommendationSources(
		List<OrderRecommendationSourceRequest> recommendationSources,
		Order order
	) {
		if (recommendationSources == null || recommendationSources.isEmpty()) {
			return List.of();
		}

		List<Long> orderedProductIds = purchasedProductIds(order);

		return recommendationSources.stream()
			.filter(Objects::nonNull)
			.filter(source -> source.productId() != null)
			.filter(source -> source.recommendationLogId() != null)
			.filter(source -> orderedProductIds.contains(source.productId()))
			.toList();
	}

	private List<Long> purchasedProductIds(Order order) {
		return order.getOrderItems().stream()
			.map(OrderItem::getProductId)
			.distinct()
			.toList();
	}

	@Transactional(readOnly = true)
	public List<OrderSummaryResponse> getOrders(Long userId) {
		return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
			.map(OrderSummaryResponse::from)
			.toList();
	}

	@Transactional(readOnly = true)
	public OrderDetailResponse getOrder(Long userId, Long orderId) {
		Order order = orderRepository.findById(orderId)
			.orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

		order.validateOwner(userId);

		return OrderDetailResponse.from(order);
	}
}