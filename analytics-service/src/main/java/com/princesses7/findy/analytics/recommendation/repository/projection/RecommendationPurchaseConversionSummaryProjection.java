package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationPurchaseConversionSummaryProjection {

	Long getImpressionCount();

	Long getClickCount();

	Long getPurchaseCount();
}