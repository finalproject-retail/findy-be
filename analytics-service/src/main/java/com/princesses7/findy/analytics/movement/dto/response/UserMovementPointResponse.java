package com.princesses7.findy.analytics.movement.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.analytics.movement.dto.query.UserMovementPointQueryResult;

public record UserMovementPointResponse(
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

	public static UserMovementPointResponse from(UserMovementPointQueryResult result) {
		return new UserMovementPointResponse(
			result.locationLogId(),
			result.zoneId(),
			result.zoneName(),
			result.gridId(),
			result.gridX(),
			result.gridY(),
			result.enteredAt(),
			result.exitedAt(),
			result.stayDurationSeconds(),
			result.sequenceNo()
		);
	}
}