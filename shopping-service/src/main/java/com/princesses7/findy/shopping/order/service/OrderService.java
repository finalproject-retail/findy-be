package com.princesses7.findy.shopping.order.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.service.CartCleanupService;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private static final Long DEFAULT_STORE_ID = 1L;

	private final PurchaseAmountService purchaseAmountService;
	private final OrderRepository orderRepository;
	private final InventoryStockService inventoryStockService;
	private final CartCleanupService cartCleanupService;

	@Transactional
	public OrderCreateResponse createOrder(Long userId) {
		return createOrder(userId, DEFAULT_STORE_ID);
	}

	@Transactional
	public OrderCreateResponse createOrder(Long userId, Long storeId) {
		PurchaseAmountResponse amountResponse = purchaseAmountService.calculate(userId);

		Order order = Order.create(
			userId,
			amountResponse.shoppingListId(),
			amountResponse.totalAmount(),
			amountResponse.discountAmount(),
			amountResponse.finalAmount()
		);

		amountResponse.items().forEach(item ->
			order.addOrderItem(createOrderItem(item))
		);

		Order savedOrder = orderRepository.save(order);

		inventoryStockService.decreaseStocks(storeId, savedOrder.getOrderItems());
		cartCleanupService.cleanupPurchasedCartItems(userId, savedOrder.getOrderItems());
		savedOrder.complete();

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