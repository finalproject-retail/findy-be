package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record ProductRecommendationListResponse(
	Long userId,
	Long sourceProductId,
	SourceProductResponse sourceProduct,
	RecommendationType recommendationType,
	List<ProductRecommendationResponse> recommendations
) {

	public ProductRecommendationListResponse withRecommendations(
		List<ProductRecommendationResponse> recommendations
	) {
		return new ProductRecommendationListResponse(
			userId,
			sourceProductId,
			sourceProduct,
			recommendationType,
			recommendations
		);
	}
}