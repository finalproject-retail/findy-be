package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationPurchaseConversionProductProjection {

	String getRecommendationType();

	Long getProductId();

	String getProductName();

	Long getImpressionCount();

	Long getClickCount();

	Long getPurchaseCount();
}