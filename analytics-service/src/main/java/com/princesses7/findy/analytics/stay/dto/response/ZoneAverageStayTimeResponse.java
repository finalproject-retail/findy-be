package com.princesses7.findy.analytics.stay.dto.response;

import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeQueryResult;

public record ZoneAverageStayTimeResponse(
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

	public static ZoneAverageStayTimeResponse from(ZoneAverageStayTimeQueryResult result) {
		return new ZoneAverageStayTimeResponse(
			result.zoneId(),
			result.zoneName(),
			result.visitCount(),
			result.uniqueVisitorCount(),
			result.averageStayDurationSeconds(),
			result.totalStayDurationSeconds(),
			result.minStayDurationSeconds(),
			result.maxStayDurationSeconds(),
			result.rankNo()
		);
	}
}