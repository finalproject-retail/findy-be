package com.princesses7.findy.analytics.zone.dto.query;

import java.math.BigDecimal;

public record ZoneMovementQueryResult(
	Long fromZoneId,
	String fromZoneName,
	Long toZoneId,
	String toZoneName,
	Long movementCount,
	BigDecimal movementRate,
	Long averageTravelTimeSeconds
) {
}