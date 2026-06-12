package com.princesses7.findy.shopping.order.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.princesses7.findy.shopping.order.entity.Order;

public record OrderSummaryResponse(
	Long orderId,
	Long shoppingListId,
	Long couponId,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	int earnedReward,
	String orderStatus,
	LocalDateTime orderedAt,
	int itemCount,
	String firstProductName,
	List<OrderItemResponse> items
) {

	public static OrderSummaryResponse from(
		Order order,
		List<OrderItemResponse> items
	) {
		return new OrderSummaryResponse(
			order.getOrderId(),
			order.getShoppingListId(),
			order.getCouponId(),
			order.getTotalAmount(),
			order.getDiscountAmount(),
			order.getFinalAmount(),
			order.getEarnedReward(),
			order.getOrderStatus().name(),
			order.getCreatedAt(),
			calculateItemCount(items),
			resolveFirstProductName(items),
			items
		);
	}

	private static int calculateItemCount(List<OrderItemResponse> items) {
		if (items == null) {
			return 0;
		}

		return items.size();
	}

	private static String resolveFirstProductName(List<OrderItemResponse> items) {
		if (items == null || items.isEmpty()) {
			return "";
		}

		OrderItemResponse firstItem = items.get(0);
		return firstItem.productName() == null ? "" : firstItem.productName();
	}
}