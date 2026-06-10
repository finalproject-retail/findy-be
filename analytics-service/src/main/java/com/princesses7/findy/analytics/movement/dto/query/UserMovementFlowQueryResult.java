package com.princesses7.findy.analytics.movement.dto.query;

import java.math.BigDecimal;

public record UserMovementFlowQueryResult(
	Long fromZoneId,
	String fromZoneName,
	Long toZoneId,
	String toZoneName,
	Long movementCount,
	BigDecimal movementRate,
	Long averageTravelTimeSeconds
) {
}