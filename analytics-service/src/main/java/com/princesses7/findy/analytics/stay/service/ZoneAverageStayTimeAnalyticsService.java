package com.princesses7.findy.analytics.stay.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.stay.dto.query.ZoneAverageStayTimeSummaryQueryResult;
import com.princesses7.findy.analytics.stay.dto.response.ZoneAverageStayTimeAnalyticsResponse;
import com.princesses7.findy.analytics.stay.dto.response.ZoneAverageStayTimeDailyResponse;
import com.princesses7.findy.analytics.stay.dto.response.ZoneAverageStayTimeResponse;
import com.princesses7.findy.analytics.stay.repository.ZoneAverageStayTimeAnalyticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneAverageStayTimeAnalyticsService {

	private static final int DEFAULT_MIN_STAY_SECONDS = 5;

	private final ZoneAverageStayTimeAnalyticsRepository zoneAverageStayTimeAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public ZoneAverageStayTimeAnalyticsResponse getZoneAverageStayTimes(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Long zoneId,
		Integer minStaySeconds,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedMinStaySeconds = resolveMinStaySeconds(minStaySeconds);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);

		ZoneAverageStayTimeSummaryQueryResult summary = zoneAverageStayTimeAnalyticsRepository.findSummary(
			periodRange,
			storeId,
			zoneId,
			resolvedMinStaySeconds
		);

		List<ZoneAverageStayTimeDailyResponse> dailyTrends = zoneAverageStayTimeAnalyticsRepository.findDailyTrends(
				periodRange,
				storeId,
				zoneId,
				resolvedMinStaySeconds
			)
			.stream()
			.map(ZoneAverageStayTimeDailyResponse::from)
			.toList();

		List<ZoneAverageStayTimeResponse> zones = zoneAverageStayTimeAnalyticsRepository.findZoneAverageStayTimes(
				periodRange,
				storeId,
				zoneId,
				resolvedMinStaySeconds,
				resolvedLimit
			)
			.stream()
			.map(ZoneAverageStayTimeResponse::from)
			.toList();

		return new ZoneAverageStayTimeAnalyticsResponse(
			PeriodResponse.from(periodRange),
			storeId,
			zoneId,
			resolvedMinStaySeconds,
			resolvedLimit,
			getTotalVisitCount(summary),
			getTotalUniqueVisitorCount(summary),
			getAverageStayDurationSeconds(summary),
			getTotalStayDurationSeconds(summary),
			dailyTrends,
			zones
		);
	}

	private int resolveMinStaySeconds(Integer minStaySeconds) {
		return minStaySeconds == null ? DEFAULT_MIN_STAY_SECONDS : minStaySeconds;
	}

	private Long getTotalVisitCount(ZoneAverageStayTimeSummaryQueryResult summary) {
		return summary == null || summary.totalVisitCount() == null
			? 0L
			: summary.totalVisitCount();
	}

	private Long getTotalUniqueVisitorCount(ZoneAverageStayTimeSummaryQueryResult summary) {
		return summary == null || summary.totalUniqueVisitorCount() == null
			? 0L
			: summary.totalUniqueVisitorCount();
	}

	private Long getAverageStayDurationSeconds(ZoneAverageStayTimeSummaryQueryResult summary) {
		return summary == null || summary.averageStayDurationSeconds() == null
			? 0L
			: summary.averageStayDurationSeconds();
	}

	private Long getTotalStayDurationSeconds(ZoneAverageStayTimeSummaryQueryResult summary) {
		return summary == null || summary.totalStayDurationSeconds() == null
			? 0L
			: summary.totalStayDurationSeconds();
	}
}