package com.princesses7.findy.analytics.congestion.dto.query;

import java.math.BigDecimal;

public record ZoneCongestionOverallSummaryQueryResult(
	BigDecimal averageUserCount,
	Long maxUserCount,
	Long congestedSlotCount,
	Long totalSlotCount
) {
}