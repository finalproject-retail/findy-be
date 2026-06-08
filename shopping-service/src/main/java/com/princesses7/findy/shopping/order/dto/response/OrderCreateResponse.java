package com.princesses7.findy.shopping.order.dto.response;

import java.util.List;

public record OrderCreateResponse(
	Long orderId,
	Long userId,
	Long shoppingListId,
	Long couponId,
	int totalAmount,
	int discountAmount,
	int usedReward,
	int finalAmount,
	String orderStatus,
	List<OrderItemResponse> items
) {
}