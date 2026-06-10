package com.princesses7.findy.analytics.congestion.dto.query;

import java.time.LocalDateTime;

public record ZoneCongestionTimeSeriesQueryResult(
	LocalDateTime timeSlot,
	Long zoneId,
	String zoneName,
	Long userCount
) {
}