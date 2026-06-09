package com.princesses7.findy.analytics.performance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.princesses7.findy.analytics.performance.dto.query.PerformanceDailyMetricQueryResult;

public record PerformanceDailyMetricResponse(
	LocalDate metricDate,
	Long visitorCount,
	Long outOfStockCount,
	Long routeUsageCount,
	Long recommendationExposureCount,
	Long recommendationPurchaseCount,
	BigDecimal recommendationConversionRate,
	Long totalSalesAmount,
	Long totalOrderCount,
	BigDecimal averageOrderAmount
) {

	public static PerformanceDailyMetricResponse of(
		PerformanceDailyMetricQueryResult result,
		BigDecimal recommendationConversionRate,
		BigDecimal averageOrderAmount
	) {
		return new PerformanceDailyMetricResponse(
			result.metricDate(),
			defaultLong(result.visitorCount()),
			defaultLong(result.outOfStockCount()),
			defaultLong(result.routeUsageCount()),
			defaultLong(result.recommendationExposureCount()),
			defaultLong(result.recommendationPurchaseCount()),
			recommendationConversionRate,
			defaultLong(result.totalSalesAmount()),
			defaultLong(result.totalOrderCount()),
			averageOrderAmount
		);
	}

	private static Long defaultLong(Long value) {
		return value == null ? 0L : value;
	}
}