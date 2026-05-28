package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateDailyProjection;

public record RecommendationClickRateDailyResponse(
	LocalDate analysisDate,
	long impressionCount,
	long clickCount,
	BigDecimal clickRate
) {

	public static RecommendationClickRateDailyResponse from(
		RecommendationClickRateDailyProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long clickCount = toLong(projection.getClickCount());

		return new RecommendationClickRateDailyResponse(
			projection.getAnalysisDate(),
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