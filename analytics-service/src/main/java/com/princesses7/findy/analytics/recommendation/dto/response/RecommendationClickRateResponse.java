package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record RecommendationClickRateResponse(
	PeriodResponse period,
	String recommendationType,
	long impressionCount,
	long clickCount,
	BigDecimal clickRate,
	List<RecommendationClickRateDailyResponse> dailyTrends,
	List<RecommendationClickRateProductResponse> products
) {

	public static RecommendationClickRateResponse of(
		PeriodResponse period,
		String recommendationType,
		long impressionCount,
		long clickCount,
		List<RecommendationClickRateDailyResponse> dailyTrends,
		List<RecommendationClickRateProductResponse> products
	) {
		return new RecommendationClickRateResponse(
			period,
			recommendationType,
			impressionCount,
			clickCount,
			calculateClickRate(impressionCount, clickCount),
			dailyTrends,
			products
		);
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