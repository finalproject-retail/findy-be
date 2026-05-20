package com.princesses7.findy.shopping.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.order.dto.response.OrderCreateResponse;
import com.princesses7.findy.shopping.order.dto.response.OrderItemResponse;
import com.princesses7.findy.shopping.order.entity.Order;
import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.order.repository.OrderRepository;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseAmountResponse;
import com.princesses7.findy.shopping.purchase.service.PurchaseAmountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final PurchaseAmountService purchaseAmountService;
	private final OrderRepository orderRepository;

	@Transactional
	public OrderCreateResponse createOrder(Long userId) {
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
}