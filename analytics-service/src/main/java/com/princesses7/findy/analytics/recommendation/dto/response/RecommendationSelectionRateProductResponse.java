package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateProductProjection;

public record RecommendationSelectionRateProductResponse(
	String recommendationType,
	Long sourceProductId,
	Long productId,
	String productName,
	long impressionCount,
	long selectionCount,
	BigDecimal selectionRate,
	long purchaseCount,
	BigDecimal conversionRate
) {

	public static RecommendationSelectionRateProductResponse from(
		RecommendationSelectionRateProductProjection projection
	) {
		long impressionCount = toLong(projection.getImpressionCount());
		long selectionCount = toLong(projection.getSelectionCount());
		long purchaseCount = toLong(projection.getPurchaseCount());

		return new RecommendationSelectionRateProductResponse(
			projection.getRecommendationType(),
			projection.getSourceProductId(),
			projection.getProductId(),
			projection.getProductName(),
			impressionCount,
			selectionCount,
			calculateRate(selectionCount, impressionCount),
			purchaseCount,
			calculateRate(purchaseCount, impressionCount)
		);
	}

	@JsonProperty("selectedCount")
	public long selectedCount() {
		return selectionCount;
	}

	@JsonProperty("selectRate")
	public BigDecimal selectRate() {
		return selectionRate;
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
