package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record RecommendationSelectionRateResponse(
	PeriodResponse period,
	String recommendationType,
	Long productId,
	Long sourceProductId,
	long impressionCount,
	long selectionCount,
	BigDecimal selectionRate,
	List<RecommendationSelectionRateDailyResponse> dailyTrends,
	List<RecommendationSelectionRateProductResponse> products
) {

	public static RecommendationSelectionRateResponse of(
		PeriodResponse period,
		String recommendationType,
		Long productId,
		Long sourceProductId,
		long impressionCount,
		long selectionCount,
		List<RecommendationSelectionRateDailyResponse> dailyTrends,
		List<RecommendationSelectionRateProductResponse> products
	) {
		return new RecommendationSelectionRateResponse(
			period,
			recommendationType,
			productId,
			sourceProductId,
			impressionCount,
			selectionCount,
			calculateRate(selectionCount, impressionCount),
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