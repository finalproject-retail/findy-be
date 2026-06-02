package com.princesses7.findy.recommendation.recommendation.log.dto.service;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record RecommendationSingleImpressionLogCommand(
	Long userId,
	Long productId,
	Long sourceProductId,
	Long storeId,
	RecommendationType recommendationType,
	String displayLocation,
	Integer recommendationRank,
	BigDecimal score,
	String reason
) {
}