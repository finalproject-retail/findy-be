package com.princesses7.findy.analytics.performance.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.performance.dto.query.PerformanceDailyMetricQueryResult;
import com.princesses7.findy.analytics.performance.dto.response.PerformancePeriodSummaryResponse;
import com.princesses7.findy.analytics.performance.dto.response.PerformanceSummaryResponse;
import com.princesses7.findy.analytics.performance.repository.PerformanceSummaryRepository;

@ExtendWith(MockitoExtension.class)
class PerformanceSummaryServiceTest {

	@Mock
	private PerformanceSummaryRepository performanceSummaryRepository;

	@Mock
	private AnalyticsPeriodResolver analyticsPeriodResolver;

	@InjectMocks
	private PerformanceSummaryService performanceSummaryService;

	@Test
	@DisplayName("성과 요약과 기간 인사이트를 조회한다")
	void getPerformanceSummary() {
		LocalDate startDate = LocalDate.of(2026, 5, 1);
		LocalDate endDate = LocalDate.of(2026, 5, 3);
		Long storeId = 1L;
		PeriodRange periodRange = PeriodRange.of(startDate, endDate);

		given(analyticsPeriodResolver.resolve(startDate, endDate))
			.willReturn(periodRange);
		given(performanceSummaryRepository.findDailyMetrics(periodRange, storeId))
			.willReturn(List.of(
				new PerformanceDailyMetricQueryResult(
					LocalDate.of(2026, 5, 1),
					10L,
					1L,
					2L,
					10L,
					1L,
					10000L,
					1L
				),
				new PerformanceDailyMetricQueryResult(
					LocalDate.of(2026, 5, 2),
					30L,
					0L,
					3L,
					20L,
					5L,
					90000L,
					2L
				),
				new PerformanceDailyMetricQueryResult(
					LocalDate.of(2026, 5, 3),
					5L,
					2L,
					1L,
					0L,
					0L,
					0L,
					0L
				)
			));

		PerformanceSummaryResponse response = performanceSummaryService.getSummary(
			startDate,
			endDate,
			storeId
		);

		assertThat(response.period().fromDate()).isEqualTo(startDate);
		assertThat(response.period().toDate()).isEqualTo(endDate);
		assertThat(response.storeId()).isEqualTo(storeId);
		assertThat(response.summary().totalVisitorCount()).isEqualTo(45L);
		assertThat(response.summary().outOfStockCount()).isEqualTo(3L);
		assertThat(response.summary().routeUsageCount()).isEqualTo(6L);
		assertThat(response.summary().recommendationConversionRate()).isEqualByComparingTo("20.00");
		assertThat(response.summary().totalSalesAmount()).isEqualTo(100000L);
		assertThat(response.summary().totalOrderCount()).isEqualTo(3L);
		assertThat(response.summary().averageOrderAmount()).isEqualByComparingTo("33333.33");
		assertThat(response.summaryText()).contains("5월 2일");
	}

	@Test
	@DisplayName("기간별 성과 요약과 일별 데이터를 조회한다")
	void getPerformancePeriodSummary() {
		LocalDate startDate = LocalDate.of(2026, 5, 1);
		LocalDate endDate = LocalDate.of(2026, 5, 2);
		PeriodRange periodRange = PeriodRange.of(startDate, endDate);

		given(analyticsPeriodResolver.resolve(startDate, endDate))
			.willReturn(periodRange);
		given(performanceSummaryRepository.findDailyMetrics(periodRange, null))
			.willReturn(List.of(
				new PerformanceDailyMetricQueryResult(
					LocalDate.of(2026, 5, 1),
					0L,
					0L,
					0L,
					0L,
					0L,
					0L,
					0L
				),
				new PerformanceDailyMetricQueryResult(
					LocalDate.of(2026, 5, 2),
					20L,
					1L,
					4L,
					8L,
					2L,
					40000L,
					1L
				)
			));

		PerformancePeriodSummaryResponse response = performanceSummaryService.getPeriodSummary(
			startDate,
			endDate,
			null
		);

		assertThat(response.dailyMetrics()).hasSize(2);
		assertThat(response.summary().totalVisitorCount()).isEqualTo(20L);
		assertThat(response.insight().highestSalesDate()).isEqualTo(LocalDate.of(2026, 5, 2));
		assertThat(response.insight().highestVisitorDate()).isEqualTo(LocalDate.of(2026, 5, 2));
	}
}