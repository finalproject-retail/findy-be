package com.princesses7.findy.analytics.stay.dto.query;

import java.time.LocalDate;

public record ZoneAverageStayTimeDailyQueryResult(
	LocalDate analysisDate,
	Long visitCount,
	Long uniqueVisitorCount,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds
) {
}