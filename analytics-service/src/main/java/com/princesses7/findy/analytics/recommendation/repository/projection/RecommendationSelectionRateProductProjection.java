package com.princesses7.findy.analytics.recommendation.repository.projection;

public interface RecommendationSelectionRateProductProjection {

	String getRecommendationType();

	Long getSourceProductId();

	Long getProductId();

	String getProductName();

	String getPromotionName();

	String getPromotionType();

	String getPromotionLabel();

	Long getImpressionCount();

	Long getSelectionCount();

	Long getPurchaseCount();
}