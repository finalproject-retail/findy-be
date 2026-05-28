package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateProductProjection;

public record RecommendationClickRateProductResponse(
	String recommendationType,
	Long productId,
	String productName,
	long impressionCount,
	long clickCount,
	BigDecimal clickRate
) {

	public static RecommendationClickRateProductResponse from(
		RecommendationClickRateProductProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long clickCount = toLong(projection.getClickCount());

		return new RecommendationClickRateProductResponse(
			projection.getRecommendationType(),
			projection.getProductId(),
			projection.getProductName(),
			impressionCount,
			clickCount,
			calculateClickRate(impressionCount, clickCount)
		);
	}

	private static long toLong(Long value) {
		return value == null ? 0L : value;
	}

	private static BigDecimal calculateClickRate(long impressionCount, long clickCount) {
		if (impressionCount <= 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(clickCount)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(impressionCount), 2, RoundingMode.HALF_UP);
	}
}