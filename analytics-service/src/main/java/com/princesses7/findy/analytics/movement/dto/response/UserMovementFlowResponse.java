package com.princesses7.findy.analytics.movement.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.analytics.movement.dto.query.UserMovementFlowQueryResult;

public record UserMovementFlowResponse(
	Long fromZoneId,
	String fromZoneName,
	Long toZoneId,
	String toZoneName,
	Long movementCount,
	BigDecimal movementRate,
	Long averageTravelTimeSeconds
) {

	public static UserMovementFlowResponse from(UserMovementFlowQueryResult result) {
		return new UserMovementFlowResponse(
			result.fromZoneId(),
			result.fromZoneName(),
			result.toZoneId(),
			result.toZoneName(),
			result.movementCount(),
			result.movementRate(),
			result.averageTravelTimeSeconds()
		);
	}
}