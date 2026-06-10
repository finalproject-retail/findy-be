package com.princesses7.findy.analytics.congestion.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionSummaryQueryResult;
import com.princesses7.findy.analytics.congestion.support.ZoneCongestionLevel;

public record ZoneCongestionResponse(
	Long zoneId,
	String zoneName,
	BigDecimal averageUserCount,
	Long maxUserCount,
	ZoneCongestionLevel peakCongestionLevel,
	String peakCongestionLevelDescription,
	Long congestedSlotCount,
	Long totalSlotCount,
	BigDecimal congestedRate
) {

	public static ZoneCongestionResponse from(ZoneCongestionSummaryQueryResult result) {
		ZoneCongestionLevel peakCongestionLevel = ZoneCongestionLevel.fromUserCount(result.maxUserCount());

		return new ZoneCongestionResponse(
			result.zoneId(),
			result.zoneName(),
			result.averageUserCount(),
			result.maxUserCount(),
			peakCongestionLevel,
			peakCongestionLevel.getDescription(),
			result.congestedSlotCount(),
			result.totalSlotCount(),
			calculateCongestedRate(result.congestedSlotCount(), result.totalSlotCount())
		);
	}

	private static BigDecimal calculateCongestedRate(Long congestedSlotCount, Long totalSlotCount) {
		if (totalSlotCount == null || totalSlotCount == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(congestedSlotCount)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(totalSlotCount), 2, RoundingMode.HALF_UP);
	}
}