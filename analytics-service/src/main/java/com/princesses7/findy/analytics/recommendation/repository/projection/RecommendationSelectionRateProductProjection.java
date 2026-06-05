package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationSelectionRateProductProjection {

	String getRecommendationType();

	Long getSourceProductId();

	Long getProductId();

	String getProductName();

	Long getImpressionCount();

	Long getSelectionCount();
}