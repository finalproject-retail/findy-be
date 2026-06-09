package com.princesses7.findy.analytics.performance.dto.query;

import java.time.LocalDate;

public record PerformanceDailyMetricQueryResult(
	LocalDate metricDate,
	Long visitorCount,
	Long outOfStockCount,
	Long routeUsageCount,
	Long recommendationExposureCount,
	Long recommendationPurchaseCount,
	Long totalSalesAmount,
	Long totalOrderCount
) {
}