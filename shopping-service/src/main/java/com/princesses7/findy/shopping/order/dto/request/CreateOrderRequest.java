package com.princesses7.findy.shopping.order.dto.request;

public record CreateOrderRequest(
	Long userCouponId,
	Integer usedReward
) {
}