package com.princesses7.findy.analytics.performance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceInsightResponse(
	String summaryText,
	LocalDate highestSalesDate,
	Long highestSalesAmount,
	LocalDate highestVisitorDate,
	Long highestVisitorCount,
	BigDecimal visitorPeakRatio
) {
}