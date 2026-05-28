package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationClickRateProductProjection {

	String getRecommendationType();

	Long getProductId();

	String getProductName();

	Long getImpressionCount();

	Long getClickCount();
}