package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionProductProjection;

public record RecommendationPurchaseConversionProductResponse(
	String recommendationType,
	Long productId,
	String productName,
	long impressionCount,
	long clickCount,
	long purchaseCount,
	BigDecimal purchaseConversionRate,
	BigDecimal clickToPurchaseRate
) {

	public static RecommendationPurchaseConversionProductResponse from(
		RecommendationPurchaseConversionProductProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long clickCount = toLong(projection.getClickCount());
		long purchaseCount = toLong(projection.getPurchaseCount());

		return new RecommendationPurchaseConversionProductResponse(
			projection.getRecommendationType(),
			projection.getProductId(),
			projection.getProductName(),
			impressionCount,
			clickCount,
			purchaseCount,
			calculateRate(purchaseCount, impressionCount),
			calculateRate(purchaseCount, clickCount)
		);
	}

	private static long toLong(Long value) {
		return value == null ? 0L : value;
	}

	private static BigDecimal calculateRate(long numerator, long denominator) {
		if (denominator <= 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(numerator)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}
}