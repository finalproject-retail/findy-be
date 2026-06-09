package com.princesses7.findy.analytics.performance.dto.response;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record PerformanceSummaryResponse(
	PeriodResponse period,
	Long storeId,
	PerformanceMetricSummaryResponse summary,
	String summaryText
) {
}