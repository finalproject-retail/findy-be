package com.princesses7.findy.recommendation.recommendation.dto.response;

import java.util.List;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record SubstituteRecommendationResponse(
	Long userId,
	Long storeId,
	Long sourceProductId,
	SourceProductResponse sourceProduct,
	SourceInventoryResponse sourceInventory,
	RecommendationType recommendationType,
	List<ProductRecommendationResponse> recommendations
) {
}