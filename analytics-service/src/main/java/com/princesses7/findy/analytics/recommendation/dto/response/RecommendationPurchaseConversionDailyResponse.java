package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionDailyProjection;

public record RecommendationPurchaseConversionDailyResponse(
	LocalDate analysisDate,
	long impressionCount,
	long clickCount,
	long purchaseCount,
	BigDecimal purchaseConversionRate,
	BigDecimal clickToPurchaseRate
) {

	public static RecommendationPurchaseConversionDailyResponse from(
		RecommendationPurchaseConversionDailyProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long clickCount = toLong(projection.getClickCount());
		long purchaseCount = toLong(projection.getPurchaseCount());

		return new RecommendationPurchaseConversionDailyResponse(
			projection.getAnalysisDate(),
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