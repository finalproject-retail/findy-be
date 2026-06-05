package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateDailyProjection;

public record RecommendationSelectionRateDailyResponse(
	LocalDate analysisDate,
	long impressionCount,
	long selectionCount,
	BigDecimal selectionRate
) {

	public static RecommendationSelectionRateDailyResponse from(
		RecommendationSelectionRateDailyProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long selectionCount = toLong(projection.getSelectionCount());

		return new RecommendationSelectionRateDailyResponse(
			projection.getAnalysisDate(),
			impressionCount,
			selectionCount,
			calculateRate(selectionCount, impressionCount)
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