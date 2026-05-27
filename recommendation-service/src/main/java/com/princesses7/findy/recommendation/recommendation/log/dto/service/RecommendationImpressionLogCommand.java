package com.princesses7.findy.recommendation.recommendation.log.dto.service;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record RecommendationImpressionLogCommand(
	Long userId,
	Long sourceProductId,
	Long storeId,
	RecommendationType recommendationType,
	String displayLocation,
	List<ProductRecommendationResponse> recommendations
) {
}