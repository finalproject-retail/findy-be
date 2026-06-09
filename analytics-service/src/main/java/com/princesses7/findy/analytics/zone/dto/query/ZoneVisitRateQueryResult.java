package com.princesses7.findy.analytics.zone.dto.query;

import java.math.BigDecimal;

public record ZoneVisitRateQueryResult(
	Long zoneId,
	String zoneName,
	Long visitCount,
	Long uniqueVisitorCount,
	BigDecimal visitRate,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds,
	Integer rankNo
) {
}