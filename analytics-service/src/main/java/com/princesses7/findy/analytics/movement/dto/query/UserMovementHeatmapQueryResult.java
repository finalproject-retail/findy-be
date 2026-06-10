package com.princesses7.findy.analytics.movement.dto.query;

public record UserMovementHeatmapQueryResult(
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
}