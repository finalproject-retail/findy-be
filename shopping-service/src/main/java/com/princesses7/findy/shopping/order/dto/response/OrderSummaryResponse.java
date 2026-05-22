package com.princesses7.findy.shopping.order.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.order.entity.Order;

public record OrderSummaryResponse(
	Long orderId,
	Long shoppingListId,
	Long couponId,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	String orderStatus,
	LocalDateTime orderedAt
) {

	public static OrderSummaryResponse from(Order order) {
		return new OrderSummaryResponse(
			order.getOrderId(),
			order.getShoppingListId(),
			order.getCouponId(),
			order.getTotalAmount(),
			order.getDiscountAmount(),
			order.getFinalAmount(),
			order.getOrderStatus().name(),
			order.getCreatedAt()
		);
	}
}