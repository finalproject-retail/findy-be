package com.princesses7.findy.analytics.analytics.dto.response;

import java.time.LocalDate;
import java.util.List;

public record ProductViewAnalyticsResponse(
	LocalDate fromDate,
	LocalDate toDate,
	Long totalViewCount,
	Long viewedProductCount,
	Integer limit,
	List<ProductViewRankingItemResponse> products
) {
}