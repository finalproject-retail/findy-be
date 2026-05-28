package com.princesses7.findy.recommendation.recommendation.service;

public record PromotionRecommendationScoreResult(
	double totalScore,
	double embeddingScore,
	double promotionBenefitScore,
	double stockScore,
	double promotionTypeScore,
	double locationScore
) {

	public boolean hasStrongAiMatch() {
		return embeddingScore >= 0.70;
	}

	public boolean hasPromotionBenefit() {
		return promotionBenefitScore > 0.0;
	}

	public boolean hasLocationSignal() {
		return locationScore > 0.0;
	}

	public boolean hasEnoughStock() {
		return stockScore >= 0.5;
	}
}