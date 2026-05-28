package com.princesses7.findy.analytics.analytics.dto.query;

public record ProductViewRankingQueryResult(
	Long productId,
	String productName,
	String brandName,
	Long categoryId,
	Long viewCount
) {
}