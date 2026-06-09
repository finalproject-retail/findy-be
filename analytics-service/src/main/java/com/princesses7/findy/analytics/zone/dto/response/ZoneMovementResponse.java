package com.princesses7.findy.analytics.zone.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.analytics.zone.dto.query.ZoneMovementQueryResult;

public record ZoneMovementResponse(
	Long fromZoneId,
	String fromZoneName,
	Long toZoneId,
	String toZoneName,
	Long movementCount,
	BigDecimal movementRate,
	Long averageTravelTimeSeconds
) {

	public static ZoneMovementResponse from(ZoneMovementQueryResult result) {
		return new ZoneMovementResponse(
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