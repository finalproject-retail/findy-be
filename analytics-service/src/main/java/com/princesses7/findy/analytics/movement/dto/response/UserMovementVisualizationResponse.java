package com.princesses7.findy.analytics.movement.dto.response;

import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record UserMovementVisualizationResponse(
	PeriodResponse period,
	Long storeId,
	Long userId,
	Integer minStaySeconds,
	Integer limit,
	Long totalUserCount,
	Long totalVisitCount,
	Long totalStayDurationSeconds,
	List<UserMovementPathResponse> paths,
	List<UserMovementHeatmapResponse> heatmaps,
	List<UserMovementFlowResponse> flows
) {
}