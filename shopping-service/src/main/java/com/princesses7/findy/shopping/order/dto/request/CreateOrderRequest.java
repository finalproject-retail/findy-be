package com.princesses7.findy.shopping.order.dto.request;

import java.util.List;

public record CreateOrderRequest(
	Long userCouponId,
	Integer usedReward,
	List<OrderRecommendationSourceRequest> recommendationSources
) {
}