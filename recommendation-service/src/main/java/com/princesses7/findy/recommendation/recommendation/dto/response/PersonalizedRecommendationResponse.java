package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationBaseType;

public record PersonalizedRecommendationResponse(
	Long userId,
	RecommendationBaseType baseType,
	List<String> preferredCategories,
	List<String> shoppingStyles,
	List<ProductRecommendationResponse> recommendations
) {
}