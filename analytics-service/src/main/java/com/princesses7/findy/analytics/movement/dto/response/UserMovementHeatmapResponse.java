package com.princesses7.findy.analytics.movement.dto.response;

import com.princesses7.findy.analytics.movement.dto.query.UserMovementHeatmapQueryResult;

public record UserMovementHeatmapResponse(
	Long zoneId,
	String zoneName,
	Long gridId,
	Integer gridX,
	Integer gridY,
	Long visitCount,
	Long uniqueVisitorCount,
	Long totalStayDurationSeconds,
	Long averageStayDurationSeconds
) {

	public static UserMovementHeatmapResponse from(UserMovementHeatmapQueryResult result) {
		return new UserMovementHeatmapResponse(
			result.zoneId(),
			result.zoneName(),
			result.gridId(),
			result.gridX(),
			result.gridY(),
			result.visitCount(),
			result.uniqueVisitorCount(),
			result.totalStayDurationSeconds(),
			result.averageStayDurationSeconds()
		);
	}
}