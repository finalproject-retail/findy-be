package com.princesses7.findy.analytics.product.dto.query;

public record ProductPerformanceSummaryQueryResult(
	Long totalProductCount,
	Long viewedProductCount,
	Long orderedProductCount,
	Long totalViewCount,
	Long totalOrderCount,
	Long totalOrderQuantity,
	Long totalSalesAmount
) {
}