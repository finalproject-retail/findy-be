package com.princesses7.findy.shopping.recommendation.dto.request;

import java.util.List;

public record RecommendationPurchaseConversionRequest(
	Long userId,
	Long orderId,
	List<Long> recommendationLogIds,
	List<Long> purchasedProductIds
) {
}