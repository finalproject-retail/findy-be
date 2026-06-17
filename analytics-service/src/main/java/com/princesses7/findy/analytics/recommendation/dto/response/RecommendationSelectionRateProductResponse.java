package com.princesses7.findy.analytics.recommendation.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateProductProjection;

public record RecommendationSelectionRateProductResponse(
	String recommendationType,
	Long sourceProductId,
	Long productId,
	String productName,
	String promotionName,
	String promotionType,
	String promotionLabel,
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
		long purchaseCount = Math.min(
			toLong(projection.getPurchaseCount()),
			selectionCount
		);

		String promotionType = normalizePromotionType(projection.getPromotionType());

		return new RecommendationSelectionRateProductResponse(
			projection.getRecommendationType(),
			projection.getSourceProductId(),
			projection.getProductId(),
			projection.getProductName(),
			projection.getPromotionName(),
			promotionType,
			resolvePromotionLabel(promotionType, projection.getPromotionLabel()),
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

	@JsonProperty("purchaseRate")
	public BigDecimal purchaseRate() {
		return conversionRate;
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

	private static String normalizePromotionType(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim().toUpperCase(Locale.ROOT);
	}

	private static String resolvePromotionLabel(String promotionType, String fallbackLabel) {
		if (fallbackLabel != null && !fallbackLabel.isBlank()) {
			return fallbackLabel.trim();
		}

		if (promotionType == null || promotionType.isBlank()) {
			return "행사";
		}

		return switch (promotionType) {
			case "ONE_PLUS_ONE", "ONE_PLUS", "ONE_PLUS_ONE_EVENT", "1_PLUS_1" -> "1+1";
			case "TWO_PLUS_ONE", "TWO_PLUS", "2_PLUS_1" -> "2+1";
			case "GIFT", "GIVEAWAY" -> "증정 행사";
			case "BUNDLE", "PACKAGE" -> "묶음 행사";
			case "COUPON" -> "쿠폰 행사";
			case "CLEARANCE" -> "마감 할인";
			case "DISCOUNT" -> "할인 행사";
			default -> "행사";
		};
	}
}