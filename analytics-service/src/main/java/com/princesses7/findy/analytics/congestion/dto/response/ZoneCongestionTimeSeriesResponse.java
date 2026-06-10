package com.princesses7.findy.analytics.congestion.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionTimeSeriesQueryResult;
import com.princesses7.findy.analytics.congestion.support.ZoneCongestionLevel;

public record ZoneCongestionTimeSeriesResponse(
	LocalDateTime timeSlot,
	Long zoneId,
	String zoneName,
	Long userCount,
	ZoneCongestionLevel congestionLevel,
	String congestionLevelDescription
) {

	public static ZoneCongestionTimeSeriesResponse from(ZoneCongestionTimeSeriesQueryResult result) {
		ZoneCongestionLevel congestionLevel = ZoneCongestionLevel.fromUserCount(result.userCount());

		return new ZoneCongestionTimeSeriesResponse(
			result.timeSlot(),
			result.zoneId(),
			result.zoneName(),
			result.userCount(),
			congestionLevel,
			congestionLevel.getDescription()
		);
	}
}