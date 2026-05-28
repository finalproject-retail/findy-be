package com.princesses7.findy.analytics.analytics.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.analytics.analytics.dto.query.ProductViewRankingQueryResult;

public record ProductViewRankingItemResponse(
	int rankNo,
	Long productId,
	String productName,
	String brandName,
	Long categoryId,
	Long viewCount,
	BigDecimal viewRate
) {

	public static ProductViewRankingItemResponse of(
		int rankNo,
		ProductViewRankingQueryResult result,
		BigDecimal viewRate
	) {
		return new ProductViewRankingItemResponse(
			rankNo,
			result.productId(),
			result.productName(),
			result.brandName(),
			result.categoryId(),
			result.viewCount(),
			viewRate
		);
	}
}