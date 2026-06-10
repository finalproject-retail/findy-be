package com.princesses7.findy.analytics.movement.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserMovementPathResponse(
	Long userId,
	Integer pointCount,
	Long totalStayDurationSeconds,
	LocalDateTime firstEnteredAt,
	LocalDateTime lastExitedAt,
	List<UserMovementPointResponse> points
) {
}