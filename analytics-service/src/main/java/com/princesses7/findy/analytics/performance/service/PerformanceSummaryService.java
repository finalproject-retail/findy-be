package com.princesses7.findy.analytics.performance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.performance.dto.query.PerformanceDailyMetricQueryResult;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceDailyMetricResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceInsightResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceMetricSummaryResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformancePeriodSummaryResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceSummaryResponse;
import com.princesses7.findy.analytics.performance.repository.PerformanceSummaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceSummaryService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
	private static final DateTimeFormatter KOREAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("M월 d일");

	private final PerformanceSummaryRepository performanceSummaryRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public PerformanceSummaryResponse getSummary(
		LocalDate startDate,
		LocalDate endDate,
		Long storeId
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(startDate, endDate);
		List<PerformanceDailyMetricResponse> dailyMetrics = getDailyMetricResponses(periodRange, storeId);
		PerformanceMetricSummaryResponse summary = summarize(dailyMetrics);
		PerformanceInsightResponse insight = createInsight(dailyMetrics);

		return new PerformanceSummaryResponse(
			PeriodResponse.from(periodRange),
			storeId,
			summary,
			insight.summaryText()
		);
	}

	public PerformancePeriodSummaryResponse getPeriodSummary(
		LocalDate startDate,
		LocalDate endDate,
		Long storeId
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(startDate, endDate);
		List<PerformanceDailyMetricResponse> dailyMetrics = getDailyMetricResponses(periodRange, storeId);
		PerformanceMetricSummaryResponse summary = summarize(dailyMetrics);
		PerformanceInsightResponse insight = createInsight(dailyMetrics);

		return new PerformancePeriodSummaryResponse(
			PeriodResponse.from(periodRange),
			storeId,
			summary,
			insight,
			dailyMetrics
		);
	}

	private List<PerformanceDailyMetricResponse> getDailyMetricResponses(
		PeriodRange periodRange,
		Long storeId
	) {
		List<PerformanceDailyMetricQueryResult> queryResults = performanceSummaryRepository.findDailyMetrics(
			periodRange,
			storeId
		);

		return queryResults.stream()
			.map(result -> PerformanceDailyMetricResponse.of(
				result,
				calculateRate(result.recommendationPurchaseCount(), result.recommendationExposureCount()),
				calculateAverage(result.totalSalesAmount(), result.totalOrderCount())
			))
			.toList();
	}

	private PerformanceMetricSummaryResponse summarize(List<PerformanceDailyMetricResponse> dailyMetrics) {
		long totalVisitorCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::visitorCount)
			.sum();

		long outOfStockCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::outOfStockCount)
			.sum();

		long routeUsageCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::routeUsageCount)
			.sum();

		long recommendationExposureCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::recommendationExposureCount)
			.sum();

		long recommendationPurchaseCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::recommendationPurchaseCount)
			.sum();

		long totalSalesAmount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::totalSalesAmount)
			.sum();

		long totalOrderCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::totalOrderCount)
			.sum();

		return new PerformanceMetricSummaryResponse(
			totalVisitorCount,
			outOfStockCount,
			routeUsageCount,
			calculateRate(recommendationPurchaseCount, recommendationExposureCount),
			totalSalesAmount,
			totalOrderCount,
			calculateAverage(totalSalesAmount, totalOrderCount)
		);
	}

	private PerformanceInsightResponse createInsight(List<PerformanceDailyMetricResponse> dailyMetrics) {
		if (dailyMetrics.isEmpty() || hasNoMetric(dailyMetrics)) {
			return new PerformanceInsightResponse(
				"선택한 기간에 집계된 성과 데이터가 없습니다.",
				null,
				0L,
				null,
				0L,
				BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
			);
		}

		PerformanceDailyMetricResponse highestSalesMetric = dailyMetrics.stream()
			.max(Comparator
				.comparingLong(PerformanceDailyMetricResponse::totalSalesAmount)
				.thenComparing(PerformanceDailyMetricResponse::metricDate))
			.orElse(null);

		PerformanceDailyMetricResponse highestVisitorMetric = dailyMetrics.stream()
			.max(Comparator
				.comparingLong(PerformanceDailyMetricResponse::visitorCount)
				.thenComparing(PerformanceDailyMetricResponse::metricDate))
			.orElse(null);

		BigDecimal averageVisitorCount = calculateAverageVisitorCount(dailyMetrics);
		BigDecimal visitorPeakRatio = calculateVisitorPeakRatio(
			highestVisitorMetric == null ? 0L : highestVisitorMetric.visitorCount(),
			averageVisitorCount
		);

		String summaryText = createSummaryText(
			highestSalesMetric,
			highestVisitorMetric,
			visitorPeakRatio
		);

		return new PerformanceInsightResponse(
			summaryText,
			highestSalesMetric == null ? null : highestSalesMetric.metricDate(),
			highestSalesMetric == null ? 0L : highestSalesMetric.totalSalesAmount(),
			highestVisitorMetric == null ? null : highestVisitorMetric.metricDate(),
			highestVisitorMetric == null ? 0L : highestVisitorMetric.visitorCount(),
			visitorPeakRatio
		);
	}

	private String createSummaryText(
		PerformanceDailyMetricResponse highestSalesMetric,
		PerformanceDailyMetricResponse highestVisitorMetric,
		BigDecimal visitorPeakRatio
	) {
		if (highestSalesMetric != null && highestSalesMetric.totalSalesAmount() > 0) {
			String highestSalesDateText = formatDate(highestSalesMetric.metricDate());

			if (highestVisitorMetric != null && highestVisitorMetric.visitorCount() > 0) {
				return "조회 기간 중 " + highestSalesDateText
					+ "에 매출이 가장 높았으며, "
					+ formatDate(highestVisitorMetric.metricDate())
					+ " 방문객은 기간 평균 대비 "
					+ visitorPeakRatio
					+ "배였습니다.";
			}

			return "조회 기간 중 " + highestSalesDateText
				+ "에 매출이 가장 높았습니다.";
		}

		if (highestVisitorMetric != null && highestVisitorMetric.visitorCount() > 0) {
			return "조회 기간 중 " + formatDate(highestVisitorMetric.metricDate())
				+ "에 방문객이 가장 많았으며, 기간 평균 대비 "
				+ visitorPeakRatio
				+ "배였습니다.";
		}

		return "선택한 기간에 집계된 성과 데이터가 없습니다.";
	}

	private boolean hasNoMetric(List<PerformanceDailyMetricResponse> dailyMetrics) {
		return dailyMetrics.stream()
			.allMatch(metric ->
				metric.visitorCount() == 0
					&& metric.outOfStockCount() == 0
					&& metric.routeUsageCount() == 0
					&& metric.recommendationExposureCount() == 0
					&& metric.recommendationPurchaseCount() == 0
					&& metric.totalSalesAmount() == 0
					&& metric.totalOrderCount() == 0
			);
	}

	private BigDecimal calculateAverageVisitorCount(List<PerformanceDailyMetricResponse> dailyMetrics) {
		if (dailyMetrics.isEmpty()) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		long totalVisitorCount = dailyMetrics.stream()
			.mapToLong(PerformanceDailyMetricResponse::visitorCount)
			.sum();

		return BigDecimal.valueOf(totalVisitorCount)
			.divide(BigDecimal.valueOf(dailyMetrics.size()), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateVisitorPeakRatio(
		Long highestVisitorCount,
		BigDecimal averageVisitorCount
	) {
		if (highestVisitorCount == null || highestVisitorCount == 0
			|| averageVisitorCount.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(highestVisitorCount)
			.divide(averageVisitorCount, 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateRate(
		Long numerator,
		Long denominator
	) {
		if (numerator == null || denominator == null || denominator == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(numerator)
			.multiply(HUNDRED)
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateAverage(
		Long totalAmount,
		Long totalCount
	) {
		if (totalAmount == null || totalCount == null || totalCount == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(totalAmount)
			.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP);
	}

	private String formatDate(LocalDate date) {
		if (date == null) {
			return "";
		}

		return date.format(KOREAN_DATE_FORMATTER);
	}
}