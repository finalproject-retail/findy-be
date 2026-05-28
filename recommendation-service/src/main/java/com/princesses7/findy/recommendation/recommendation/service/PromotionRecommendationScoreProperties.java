package com.princesses7.findy.recommendation.recommendation.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recommendation.promotion.score")
public record PromotionRecommendationScoreProperties(
	double embeddingWeight,
	double promotionBenefitWeight,
	double stockWeight,
	double promotionTypeWeight,
	double locationWeight,
	double fallbackBaseScore,
	double fallbackBenefitWeight,
	double fallbackStockWeight,
	double fallbackPromotionTypeWeight,
	double fallbackLocationWeight,
	int nearGridDistance,
	int displayableGridDistance
) {

	public PromotionRecommendationScoreProperties {
		embeddingWeight = defaultIfNotPositive(embeddingWeight, 0.70);
		promotionBenefitWeight = defaultIfNotPositive(promotionBenefitWeight, 0.15);
		stockWeight = defaultIfNotPositive(stockWeight, 0.05);
		promotionTypeWeight = defaultIfNotPositive(promotionTypeWeight, 0.05);
		locationWeight = defaultIfNotPositive(locationWeight, 0.05);

		fallbackBaseScore = defaultIfNotPositive(fallbackBaseScore, 0.30);
		fallbackBenefitWeight = defaultIfNotPositive(fallbackBenefitWeight, 0.35);
		fallbackStockWeight = defaultIfNotPositive(fallbackStockWeight, 0.15);
		fallbackPromotionTypeWeight = defaultIfNotPositive(fallbackPromotionTypeWeight, 0.15);
		fallbackLocationWeight = defaultIfNotPositive(fallbackLocationWeight, 0.05);

		nearGridDistance = defaultIfNotPositive(nearGridDistance, 2);
		displayableGridDistance = defaultIfNotPositive(displayableGridDistance, 5);
	}

	public double activeWeightSum() {
		return embeddingWeight
			+ promotionBenefitWeight
			+ stockWeight
			+ promotionTypeWeight
			+ locationWeight;
	}

	private static double defaultIfNotPositive(
		double value,
		double defaultValue
	) {
		if (value <= 0) {
			return defaultValue;
		}

		return value;
	}

	private static int defaultIfNotPositive(
		int value,
		int defaultValue
	) {
		if (value <= 0) {
			return defaultValue;
		}

		return value;
	}
}