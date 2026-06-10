package com.princesses7.findy.analytics.congestion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.congestion.dto.query.ZoneCongestionOverallSummaryQueryResult;
import com.princesses7.findy.analytics.congestion.dto.response.ZoneCongestionAnalyticsResponse;
import com.princesses7.findy.analytics.congestion.dto.response.ZoneCongestionResponse;
import com.princesses7.findy.analytics.congestion.dto.response.ZoneCongestionTimeSeriesResponse;
import com.princesses7.findy.analytics.congestion.repository.ZoneCongestionAnalyticsRepository;
import com.princesses7.findy.analytics.congestion.support.ZoneCongestionLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneCongestionAnalyticsService {

	// TODO: 테스트용으로 임의 설정한 값이므로 추후 수정 필요
	private static final int DEFAULT_INTERVAL_MINUTES = 1;

	private final AnalyticsPeriodResolver analyticsPeriodResolver;
	private final ZoneCongestionAnalyticsRepository zoneCongestionAnalyticsRepository;

	public ZoneCongestionAnalyticsResponse getZoneCongestion(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Long zoneId,
		Integer intervalMinutes,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedIntervalMinutes = resolveIntervalMinutes(intervalMinutes);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);

		ZoneCongestionOverallSummaryQueryResult overallSummary = zoneCongestionAnalyticsRepository.findOverallSummary(
			periodRange,
			storeId,
			zoneId,
			resolvedIntervalMinutes
		);

		List<ZoneCongestionResponse> zones = zoneCongestionAnalyticsRepository.findZoneSummaries(
				periodRange,
				storeId,
				zoneId,
				resolvedIntervalMinutes,
				resolvedLimit
			)
			.stream()
			.map(ZoneCongestionResponse::from)
			.toList();

		List<ZoneCongestionTimeSeriesResponse> timeSeries = zoneCongestionAnalyticsRepository.findTimeSeries(
				periodRange,
				storeId,
				zoneId,
				resolvedIntervalMinutes,
				resolvedLimit
			)
			.stream()
			.map(ZoneCongestionTimeSeriesResponse::from)
			.toList();

		Long maxUserCount = getMaxUserCount(overallSummary);
		ZoneCongestionLevel peakCongestionLevel = ZoneCongestionLevel.fromUserCount(maxUserCount);

		return new ZoneCongestionAnalyticsResponse(
			PeriodResponse.from(periodRange),
			storeId,
			zoneId,
			resolvedIntervalMinutes,
			resolvedLimit,
			getAverageUserCount(overallSummary),
			maxUserCount,
			peakCongestionLevel,
			peakCongestionLevel.getDescription(),
			getCongestedSlotCount(overallSummary),
			getTotalSlotCount(overallSummary),
			calculateCongestedRate(getCongestedSlotCount(overallSummary), getTotalSlotCount(overallSummary)),
			zones,
			timeSeries
		);
	}

	private int resolveIntervalMinutes(Integer intervalMinutes) {
		return intervalMinutes == null ? DEFAULT_INTERVAL_MINUTES : intervalMinutes;
	}

	private BigDecimal getAverageUserCount(ZoneCongestionOverallSummaryQueryResult summary) {
		if (summary == null || summary.averageUserCount() == null) {
			return BigDecimal.ZERO;
		}

		return summary.averageUserCount();
	}

	private Long getMaxUserCount(ZoneCongestionOverallSummaryQueryResult summary) {
		if (summary == null || summary.maxUserCount() == null) {
			return 0L;
		}

		return summary.maxUserCount();
	}

	private Long getCongestedSlotCount(ZoneCongestionOverallSummaryQueryResult summary) {
		if (summary == null || summary.congestedSlotCount() == null) {
			return 0L;
		}

		return summary.congestedSlotCount();
	}

	private Long getTotalSlotCount(ZoneCongestionOverallSummaryQueryResult summary) {
		if (summary == null || summary.totalSlotCount() == null) {
			return 0L;
		}

		return summary.totalSlotCount();
	}

	private BigDecimal calculateCongestedRate(Long congestedSlotCount, Long totalSlotCount) {
		if (totalSlotCount == null || totalSlotCount == 0) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(congestedSlotCount)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(totalSlotCount), 2, RoundingMode.HALF_UP);
	}
}