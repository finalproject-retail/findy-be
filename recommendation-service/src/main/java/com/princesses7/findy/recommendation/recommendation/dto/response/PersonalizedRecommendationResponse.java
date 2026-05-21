package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

public record PersonalizedRecommendationResponse(
	Long userId,
	String baseType,
	List<String> preferredCategories,
	List<String> shoppingStyles,
	List<ProductRecommendationResponse> recommendations
) {
}