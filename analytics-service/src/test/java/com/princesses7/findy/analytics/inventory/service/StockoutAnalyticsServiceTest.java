package com.princesses7.findy.analytics.inventory.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutDailyQueryResult;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutProductQueryResult;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutSummaryQueryResult;
import com.princesses7.findy.analytics.inventory.dto.response.StockoutAnalyticsResponse;
import com.princesses7.findy.analytics.inventory.repository.StockoutAnalyticsRepository;

@ExtendWith(MockitoExtension.class)
class StockoutAnalyticsServiceTest {

	@Mock
	private StockoutAnalyticsRepository stockoutAnalyticsRepository;

	@Mock
	private AnalyticsPeriodResolver analyticsPeriodResolver;

	@InjectMocks
	private StockoutAnalyticsService stockoutAnalyticsService;

	@Test
	@DisplayName("품절 발생 현황 요약, 일별 추이, 상품 목록을 조회한다")
	void getStockoutAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Long storeId = 1L;
		Long categoryId = 17L;
		Integer limit = 10;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(limit);

		given(stockoutAnalyticsRepository.findSummary(periodRange, storeId, categoryId))
			.willReturn(new StockoutSummaryQueryResult(
				20L,
				3L,
				4L,
				2L
			));

		given(stockoutAnalyticsRepository.findDailyTrends(periodRange, storeId, categoryId))
			.willReturn(List.of(
				new StockoutDailyQueryResult(LocalDate.of(2026, 5, 1), 1L, 1L),
				new StockoutDailyQueryResult(LocalDate.of(2026, 5, 2), 3L, 2L)
			));

		given(stockoutAnalyticsRepository.findStockoutProducts(periodRange, storeId, categoryId, limit))
			.willReturn(List.of(
				new StockoutProductQueryResult(
					10001L,
					"시드_신라면",
					"농심",
					17L,
					"라면",
					1L,
					0,
					"OUT_OF_STOCK",
					LocalDateTime.of(2026, 5, 2, 10, 0)
				)
			));

		StockoutAnalyticsResponse response = stockoutAnalyticsService.getStockoutAnalytics(
			fromDate,
			toDate,
			storeId,
			categoryId,
			limit
		);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.storeId()).isEqualTo(storeId);
		assertThat(response.categoryId()).isEqualTo(categoryId);
		assertThat(response.totalInventoryProductCount()).isEqualTo(20L);
		assertThat(response.stockoutProductCount()).isEqualTo(3L);
		assertThat(response.stockoutOccurrenceCount()).isEqualTo(4L);
		assertThat(response.lowStockProductCount()).isEqualTo(2L);
		assertThat(response.stockoutRate()).isEqualByComparingTo("15.00");
		assertThat(response.limit()).isEqualTo(10);
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.products()).hasSize(1);
		assertThat(response.products().get(0).rankNo()).isEqualTo(1);
		assertThat(response.products().get(0).stockStatus()).isEqualTo("OUT_OF_STOCK");
	}

	@Test
	@DisplayName("재고 상품 수가 0이면 품절률은 0으로 반환한다")
	void getZeroStockoutRateWhenTotalInventoryProductCountIsZero() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Integer limit = 10;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(limit);

		given(stockoutAnalyticsRepository.findSummary(periodRange, null, null))
			.willReturn(new StockoutSummaryQueryResult(
				0L,
				0L,
				0L,
				0L
			));

		given(stockoutAnalyticsRepository.findDailyTrends(periodRange, null, null))
			.willReturn(List.of());

		given(stockoutAnalyticsRepository.findStockoutProducts(periodRange, null, null, limit))
			.willReturn(List.of());

		StockoutAnalyticsResponse response = stockoutAnalyticsService.getStockoutAnalytics(
			fromDate,
			toDate,
			null,
			null,
			limit
		);

		assertThat(response.totalInventoryProductCount()).isZero();
		assertThat(response.stockoutProductCount()).isZero();
		assertThat(response.stockoutOccurrenceCount()).isZero();
		assertThat(response.stockoutRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).isEmpty();
		assertThat(response.products()).isEmpty();
	}
}