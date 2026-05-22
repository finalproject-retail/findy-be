package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record ProductRecommendationListResponse(
	Long userId,
	Long sourceProductId,
	RecommendationType recommendationType,
	List<ProductRecommendationResponse> recommendations
) {
}