package com.princesses7.findy.analytics.performance.dto.response;

import java.math.BigDecimal;

public record PerformanceMetricSummaryResponse(
	Long totalVisitorCount,
	Long outOfStockCount,
	Long routeUsageCount,
	BigDecimal recommendationConversionRate,
	Long totalSalesAmount,
	Long totalOrderCount,
	BigDecimal averageOrderAmount
) {
}