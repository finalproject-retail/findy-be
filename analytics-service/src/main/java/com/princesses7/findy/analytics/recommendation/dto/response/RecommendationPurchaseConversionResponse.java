package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record RecommendationPurchaseConversionResponse(
	PeriodResponse period,
	String recommendationType,
	Long productId,
	long impressionCount,
	long clickCount,
	long purchaseCount,
	BigDecimal purchaseConversionRate,
	BigDecimal clickToPurchaseRate,
	List<RecommendationPurchaseConversionDailyResponse> dailyTrends,
	List<RecommendationPurchaseConversionProductResponse> products
) {

	public static RecommendationPurchaseConversionResponse of(
		PeriodResponse period,
		String recommendationType,
		Long productId,
		long impressionCount,
		long clickCount,
		long purchaseCount,
		List<RecommendationPurchaseConversionDailyResponse> dailyTrends,
		List<RecommendationPurchaseConversionProductResponse> products
	) {
		return new RecommendationPurchaseConversionResponse(
			period,
			recommendationType,
			productId,
			impressionCount,
			clickCount,
			purchaseCount,
			calculateRate(purchaseCount, impressionCount),
			calculateRate(purchaseCount, clickCount),
			dailyTrends,
			products
		);
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