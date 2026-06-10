package com.princesses7.findy.analytics.stay.dto.query;

public record ZoneAverageStayTimeQueryResult(
	Long zoneId,
	String zoneName,
	Long visitCount,
	Long uniqueVisitorCount,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds,
	Long minStayDurationSeconds,
	Long maxStayDurationSeconds,
	Integer rankNo
) {
}