package com.princesses7.findy.analytics.performance.dto.response;

import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record PerformancePeriodSummaryResponse(
	PeriodResponse period,
	Long storeId,
	PerformanceMetricSummaryResponse summary,
	PerformanceInsightResponse insight,
	List<PerformanceDailyMetricResponse> dailyMetrics
) {
}