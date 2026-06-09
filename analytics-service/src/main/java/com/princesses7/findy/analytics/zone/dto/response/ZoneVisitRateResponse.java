package com.princesses7.findy.analytics.zone.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.analytics.zone.dto.query.ZoneVisitRateQueryResult;

public record ZoneVisitRateResponse(
	Long zoneId,
	String zoneName,
	Long visitCount,
	Long uniqueVisitorCount,
	BigDecimal visitRate,
	Long averageStayDurationSeconds,
	Long totalStayDurationSeconds,
	Integer rankNo
) {

	public static ZoneVisitRateResponse from(ZoneVisitRateQueryResult result) {
		return new ZoneVisitRateResponse(
			result.zoneId(),
			result.zoneName(),
			result.visitCount(),
			result.uniqueVisitorCount(),
			result.visitRate(),
			result.averageStayDurationSeconds(),
			result.totalStayDurationSeconds(),
			result.rankNo()
		);
	}
}