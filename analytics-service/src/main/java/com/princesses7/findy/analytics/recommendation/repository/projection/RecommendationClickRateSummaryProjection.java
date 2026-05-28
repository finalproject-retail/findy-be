package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationClickRateSummaryProjection {

	Long getImpressionCount();

	Long getClickCount();
}