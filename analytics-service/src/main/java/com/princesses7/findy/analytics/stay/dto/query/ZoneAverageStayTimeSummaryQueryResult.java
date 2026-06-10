package com.princesses7.findy.analytics.stay.dto.query;

public record ZoneAverageStayTimeSummaryQueryResult(
	Long totalVisitCount,
	Long totalUniqueVisitorCount,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds
) {
}