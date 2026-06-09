package com.princesses7.findy.analytics.zone.dto.response;

import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record ZoneVisitRateAnalyticsResponse(
	PeriodResponse period,
	Long storeId,
	Long zoneId,
	Integer minStaySeconds,
	Boolean includeMovement,
	List<ZoneVisitRateResponse> zoneVisitRates,
	List<ZoneMovementResponse> zoneMovements
) {
}