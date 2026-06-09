package com.princesses7.findy.analytics.zone.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.zone.dto.response.ZoneMovementResponse;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateAnalyticsResponse;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateResponse;
import com.princesses7.findy.analytics.zone.repository.ZoneVisitRateAnalyticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneVisitRateAnalyticsService {

	private static final int DEFAULT_MIN_STAY_SECONDS = 5;

	private final ZoneVisitRateAnalyticsRepository zoneVisitRateAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public ZoneVisitRateAnalyticsResponse getZoneVisitRates(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Long zoneId,
		Integer minStaySeconds,
		Boolean includeMovement
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedMinStaySeconds = resolveMinStaySeconds(minStaySeconds);
		boolean resolvedIncludeMovement = Boolean.TRUE.equals(includeMovement);

		List<ZoneVisitRateResponse> zoneVisitRates = zoneVisitRateAnalyticsRepository.findZoneVisitRates(
				periodRange,
				storeId,
				zoneId,
				resolvedMinStaySeconds
			)
			.stream()
			.map(ZoneVisitRateResponse::from)
			.toList();

		List<ZoneMovementResponse> zoneMovements = resolvedIncludeMovement
			? zoneVisitRateAnalyticsRepository.findZoneMovements(periodRange, storeId, zoneId, resolvedMinStaySeconds)
			  .stream()
			  .map(ZoneMovementResponse::from)
			  .toList()
			: List.of();

		return new ZoneVisitRateAnalyticsResponse(
			PeriodResponse.from(periodRange),
			storeId,
			zoneId,
			resolvedMinStaySeconds,
			resolvedIncludeMovement,
			zoneVisitRates,
			zoneMovements
		);
	}

	private int resolveMinStaySeconds(Integer minStaySeconds) {
		if (minStaySeconds == null) {
			return DEFAULT_MIN_STAY_SECONDS;
		}

		return minStaySeconds;
	}
}