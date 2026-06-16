package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record RecommendationSelectionRateResponse(
	PeriodResponse period,
	String recommendationType,
	Long productId,
	Long sourceProductId,
	long impressionCount,
	long selectionCount,
	BigDecimal selectionRate,
	long purchaseCount,
	BigDecimal conversionRate,
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
		long purchaseCount,
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
			purchaseCount,
			calculateRate(purchaseCount, impressionCount),
			dailyTrends,
			products
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

	@JsonProperty("promotionSelectRates")
	public List<RecommendationSelectionRateProductResponse> promotionSelectRates() {
		if ("PROMOTION".equalsIgnoreCase(recommendationType)) {
			return products;
		}

		return List.of();
	}

	@JsonProperty("alternativeSelectRates")
	public List<RecommendationSelectionRateProductResponse> alternativeSelectRates() {
		if ("SUBSTITUTE".equalsIgnoreCase(recommendationType)) {
			return products;
		}

		return List.of();
	}

	@JsonProperty("substituteSelectRates")
	public List<RecommendationSelectionRateProductResponse> substituteSelectRates() {
		if ("SUBSTITUTE".equalsIgnoreCase(recommendationType)) {
			return products;
		}

		return List.of();
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
