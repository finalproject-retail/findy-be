package com.princesses7.findy.shopping.order.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
import com.princesses7.findy.shopping.cart.service.CartCleanupService;
import com.princesses7.findy.shopping.coupon.dto.response.CouponDiscountResult;
import com.princesses7.findy.shopping.coupon.service.CouponService;
import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderDetailResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderItemResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderSummaryResponse;
import com.princesses7.findy.shopping.order.entity.Order;
import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.order.exception.OrderException;
import com.princesses7.findy.shopping.order.repository.OrderRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.service.ProductSummaryReader;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountResponse;
import com.princesses7.findy.shopping.purchase.service.PurchaseAmountService;
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
	private final ProductSummaryReader productSummaryReader;

	@Transactional
	public OrderCreateResponse createOrder(
		Long userId,
		Long userCouponId,
		Integer usedReward,
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

		cartCleanupService.cleanupPurchasedCartItems(userId, savedOrder.getOrderItems());
		couponService.useCoupon(userId, userCouponId);
		shoppingListService.completeShopping(userId, resolvedStoreId);

		savedOrder.complete();

		shoppingAnalyticsEventService.publishOrderCompleted(userId, savedOrder);

		return toResponse(savedOrder, resolvedStoreId);
	}

	@Transactional(readOnly = true)
	public List<OrderSummaryResponse> getOrders(
		Long userId,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		List<Order> orders = orderRepository.findAllWithItemsByUserIdOrderByCreatedAtDesc(userId);

		Map<Long, ProductSummaryResponse> productSummaryMap = getProductSummaryMapByOrders(
			orders,
			resolvedStoreId
		);

		return orders.stream()
			.map(order -> OrderSummaryResponse.from(
				order,
				toItemResponses(order, productSummaryMap)
			))
			.toList();
	}

	@Transactional(readOnly = true)
	public OrderDetailResponse getOrder(
		Long userId,
		Long orderId,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		Order order = orderRepository.findByIdWithItems(orderId)
			.orElseThrow(() -> new OrderException(ORDER_NOT_FOUND));

		order.validateOwner(userId);

		Map<Long, ProductSummaryResponse> productSummaryMap = getProductSummaryMap(
			order.getOrderItems(),
			resolvedStoreId
		);

		return OrderDetailResponse.from(
			order,
			toItemResponses(order, productSummaryMap)
		);
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

	private OrderCreateResponse toResponse(
		Order order,
		long storeId
	) {
		Map<Long, ProductSummaryResponse> productSummaryMap = getProductSummaryMap(
			order.getOrderItems(),
			storeId
		);

		List<OrderItemResponse> items = toItemResponses(order, productSummaryMap);

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

	private List<OrderItemResponse> toItemResponses(
		Order order,
		Map<Long, ProductSummaryResponse> productSummaryMap
	) {
		return order.getOrderItems().stream()
			.sorted(Comparator.comparing(OrderItem::getOrderItemId))
			.map(item -> OrderItemResponse.from(
				item,
				productSummaryMap.get(item.getProductId())
			))
			.toList();
	}

	private Map<Long, ProductSummaryResponse> getProductSummaryMapByOrders(
		List<Order> orders,
		long storeId
	) {
		List<OrderItem> orderItems = orders.stream()
			.flatMap(order -> order.getOrderItems().stream())
			.toList();

		return getProductSummaryMap(orderItems, storeId);
	}

	private Map<Long, ProductSummaryResponse> getProductSummaryMap(
		List<OrderItem> orderItems,
		long storeId
	) {
		List<Long> productIds = orderItems.stream()
			.map(OrderItem::getProductId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (productIds.isEmpty()) {
			return Map.of();
		}

		return productSummaryReader.getExistingProductSummaries(productIds, storeId)
			.stream()
			.collect(Collectors.toMap(
				ProductSummaryResponse::productId,
				Function.identity(),
				(existingProduct, replacementProduct) -> existingProduct
			));
	}

	private int normalizeUsedReward(Integer usedReward) {
		if (usedReward == null || usedReward <= 0) {
			return 0;
		}

		return usedReward;
	}
}