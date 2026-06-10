package com.princesses7.findy.analytics.stay.dto.response;

import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record ZoneAverageStayTimeAnalyticsResponse(
	PeriodResponse period,
	Long storeId,
	Long zoneId,
	Integer minStaySeconds,
	Integer limit,
	Long totalVisitCount,
	Long totalUniqueVisitorCount,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds,
	List<ZoneAverageStayTimeDailyResponse> dailyTrends,
	List<ZoneAverageStayTimeResponse> zones
) {
}