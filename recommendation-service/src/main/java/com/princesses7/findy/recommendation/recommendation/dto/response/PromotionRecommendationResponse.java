package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record PromotionRecommendationResponse(
	Long userId,
	Long storeId,
	List<String> preferredCategories,
	List<String> shoppingStyles,
	RecommendationType recommendationType,
	List<PromotionProductRecommendationResponse> recommendations
) {
}