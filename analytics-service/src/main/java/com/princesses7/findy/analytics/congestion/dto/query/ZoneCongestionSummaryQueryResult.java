package com.princesses7.findy.analytics.congestion.dto.query;

import java.math.BigDecimal;

public record ZoneCongestionSummaryQueryResult(
	Long zoneId,
	String zoneName,
	BigDecimal averageUserCount,
	Long maxUserCount,
	Long congestedSlotCount,
	Long totalSlotCount
) {
}