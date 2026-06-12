package com.princesses7.findy.shopping.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.princesses7.findy.shopping.order.entity.Order;

public record OrderDetailResponse(
	Long orderId,
	Long userId,
	Long shoppingListId,
	Long couponId,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	int earnedReward,
	String orderStatus,
	LocalDateTime orderedAt,
	List<OrderItemResponse> items
) {

	public static OrderDetailResponse from(
		Order order,
		List<OrderItemResponse> items
	) {
		return new OrderDetailResponse(
			order.getOrderId(),
			order.getUserId(),
			order.getShoppingListId(),
			order.getCouponId(),
			order.getTotalAmount(),
			order.getDiscountAmount(),
			order.getFinalAmount(),
			order.getEarnedReward(),
			order.getOrderStatus().name(),
			order.getCreatedAt(),
			items
		);
	}
}