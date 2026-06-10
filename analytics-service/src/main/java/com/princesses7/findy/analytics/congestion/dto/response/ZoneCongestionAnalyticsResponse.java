package com.princesses7.findy.analytics.congestion.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.congestion.support.ZoneCongestionLevel;

public record ZoneCongestionAnalyticsResponse(
	PeriodResponse period,
	Long storeId,
	Long zoneId,
	Integer intervalMinutes,
	Integer limit,
	BigDecimal averageUserCount,
	Long maxUserCount,
	ZoneCongestionLevel peakCongestionLevel,
	String peakCongestionLevelDescription,
	Long congestedSlotCount,
	Long totalSlotCount,
	BigDecimal congestedRate,
	List<ZoneCongestionResponse> zones,
	List<ZoneCongestionTimeSeriesResponse> timeSeries
) {
}