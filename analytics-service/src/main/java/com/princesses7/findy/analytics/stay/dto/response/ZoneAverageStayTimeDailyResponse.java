package com.princesses7.findy.analytics.stay.dto.response;

import java.time.LocalDate;

import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeDailyQueryResult;

public record ZoneAverageStayTimeDailyResponse(
	LocalDate analysisDate,
	Long visitCount,
	Long uniqueVisitorCount,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds
) {

	public static ZoneAverageStayTimeDailyResponse from(ZoneAverageStayTimeDailyQueryResult result) {
		return new ZoneAverageStayTimeDailyResponse(
			result.analysisDate(),
			result.visitCount(),
			result.uniqueVisitorCount(),
			result.averageStayDurationSeconds(),
			result.totalStayDurationSeconds()
		);
	}
}