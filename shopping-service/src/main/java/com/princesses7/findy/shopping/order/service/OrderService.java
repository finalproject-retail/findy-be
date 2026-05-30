package com.princesses7.findy.shopping.order.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.service.CartCleanupService;
import com.princesses7.findy.shopping.coupon.dto.response.CouponDiscountResult;
import com.princesses7.findy.shopping.coupon.service.CouponService;
import com.princesses7.findy.shopping.inventory.service.InventoryStockService;
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
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.repository.ShoppingListRepository;
import com.princesses7.findy.shopping.shoppinglist.service.ShoppingListService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private static final Long DEFAULT_STORE_ID = 1L;

	private final PurchaseAmountService purchaseAmountService;
	private final OrderRepository orderRepository;
	private final CartCleanupService cartCleanupService;
	private final CouponService couponService;
	private final ShoppingListService shoppingListService;
	private final ShoppingListRepository shoppingListRepository;
	private final InventoryStockService inventoryStockService;

	@Transactional
	public OrderCreateResponse createOrder(Long userId) {
		return createOrder(userId, null, DEFAULT_STORE_ID);
	}

	@Transactional
	public OrderCreateResponse createOrder(Long userId, Long userCouponId) {
		return createOrder(userId, userCouponId, DEFAULT_STORE_ID);
	}

	@Transactional
	public OrderCreateResponse createOrder(
		Long userId,
		Long userCouponId,
		Long storeId
	) {
		PurchaseAmountResponse amountResponse = purchaseAmountService.calculate(userId);

		CouponDiscountResult couponDiscount = couponService.applyCoupon(
			userId,
			userCouponId,
			amountResponse.finalAmount()
		);

		int totalDiscountAmount = amountResponse.discountAmount() + couponDiscount.discountAmount();
		int finalAmount = amountResponse.finalAmount() - couponDiscount.discountAmount();

		Order order = Order.create(
			userId,
			amountResponse.shoppingListId(),
			couponDiscount.couponId(),
			amountResponse.totalAmount(),
			totalDiscountAmount,
			finalAmount
		);

		amountResponse.items().forEach(item ->
			order.addOrderItem(createOrderItem(item))
		);

		Order savedOrder = orderRepository.save(order);

		cartCleanupService.cleanupPurchasedCartItems(userId, savedOrder.getOrderItems());
		couponService.useCoupon(userId, userCouponId);
		shoppingListService.completeShopping(userId);

		savedOrder.complete();

		return toResponse(savedOrder);
	}

	@Transactional
	public void completeShopping(Long userId) {
		ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_NOT_FOUND));

		inventoryStockService.increaseUnscannedStocksByShoppingListItems(
			DEFAULT_STORE_ID,
			shoppingList.getShoppingListItems()
		);

		shoppingList.cancel();
		shoppingListRepository.delete(shoppingList);
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
			order.getFinalAmount(),
			order.getOrderStatus().name(),
			items
		);
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