package com.princesses7.findy.analytics.movement.dto.query;

import java.time.LocalDateTime;

public record UserMovementPointQueryResult(
	Long userId,
	Long locationLogId,
	Long zoneId,
	String zoneName,
	Long gridId,
	Integer gridX,
	Integer gridY,
	LocalDateTime enteredAt,
	LocalDateTime exitedAt,
	Long stayDurationSeconds,
	Integer sequenceNo
) {
}