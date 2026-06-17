package com.princesses7.findy.analytics.product.service;

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
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceProductQueryResult;
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceSummaryQueryResult;
import com.princesses7.findy.analytics.product.dto.response.ProductPerformanceSummaryResponse;
import com.princesses7.findy.analytics.product.repository.ProductPerformanceSummaryRepository;

@ExtendWith(MockitoExtension.class)
class ProductPerformanceSummaryServiceTest {

	@Mock
	private ProductPerformanceSummaryRepository productPerformanceSummaryRepository;

	@Mock
	private AnalyticsPeriodResolver analyticsPeriodResolver;

	@InjectMocks
	private ProductPerformanceSummaryService productPerformanceSummaryService;

	@Test
	@DisplayName("카테고리 필터를 적용해 상품 성과 요약과 상위 상품 성과를 조회한다")
	void getProductPerformanceSummary() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Long storeId = 1L;
		List<Long> categoryIds = List.of(17L, 18L);
		Integer limit = 10;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(limit);
		given(productPerformanceSummaryRepository.findSummary(periodRange, storeId, categoryIds))
			.willReturn(new ProductPerformanceSummaryQueryResult(
				20L,
				2L,
				2L,
				10L,
				3L,
				4L,
				27000L
			));
		given(productPerformanceSummaryRepository.findTopProducts(periodRange, storeId, categoryIds, limit))
			.willReturn(List.of(
				new ProductPerformanceProductQueryResult(
					10001L,
					"신라면",
					"농심",
					17L,
					"라면",
					12L,
					2L,
					6L,
					18000L
				),
				new ProductPerformanceProductQueryResult(
					10003L,
					"백미밥",
					"햇반",
					18L,
					"즉석밥",
					10L,
					1L,
					3L,
					9000L
				)
			));

		ProductPerformanceSummaryResponse response = productPerformanceSummaryService.getProductPerformanceSummary(
			fromDate,
			toDate,
			storeId,
			categoryIds,
			limit
		);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.storeId()).isEqualTo(storeId);
		assertThat(response.totalProductCount()).isEqualTo(20L);
		assertThat(response.viewedProductCount()).isEqualTo(2L);
		assertThat(response.orderedProductCount()).isEqualTo(2L);
		assertThat(response.totalViewCount()).isEqualTo(10L);
		assertThat(response.totalOrderCount()).isEqualTo(3L);
		assertThat(response.totalOrderQuantity()).isEqualTo(4L);
		assertThat(response.totalSalesAmount()).isEqualTo(27000L);
		assertThat(response.purchaseConversionRate()).isEqualByComparingTo("30.00");
		assertThat(response.averageSalesAmountPerOrder()).isEqualByComparingTo("9000.00");
		assertThat(response.limit()).isEqualTo(limit);
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).rankNo()).isEqualTo(1);
		assertThat(response.products().get(0).viewToPurchaseRate()).isEqualByComparingTo("16.67");
		assertThat(response.products().get(0).salesShareRate()).isEqualByComparingTo("66.67");
	}

	@Test
	@DisplayName("성과 데이터가 없으면 지표를 0으로 반환한다")
	void getEmptyProductPerformanceSummary() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 1);
		Long storeId = null;
		List<Long> categoryIds = null;
		List<Long> resolvedCategoryIds = List.of();
		Integer limit = null;
		int defaultLimit = 100;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(defaultLimit);
		given(productPerformanceSummaryRepository.findSummary(periodRange, storeId, resolvedCategoryIds))
			.willReturn(new ProductPerformanceSummaryQueryResult(
				0L,
				0L,
				0L,
				0L,
				0L,
				0L,
				0L
			));
		given(productPerformanceSummaryRepository.findTopProducts(periodRange, storeId, resolvedCategoryIds, defaultLimit))
			.willReturn(List.of());

		ProductPerformanceSummaryResponse response = productPerformanceSummaryService.getProductPerformanceSummary(
			fromDate,
			toDate,
			storeId,
			categoryIds,
			limit
		);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.storeId()).isNull();
		assertThat(response.totalProductCount()).isZero();
		assertThat(response.viewedProductCount()).isZero();
		assertThat(response.orderedProductCount()).isZero();
		assertThat(response.totalViewCount()).isZero();
		assertThat(response.totalOrderCount()).isZero();
		assertThat(response.totalOrderQuantity()).isZero();
		assertThat(response.totalSalesAmount()).isZero();
		assertThat(response.purchaseConversionRate()).isEqualByComparingTo("0.00");
		assertThat(response.averageSalesAmountPerOrder()).isEqualByComparingTo("0.00");
		assertThat(response.limit()).isEqualTo(defaultLimit);
		assertThat(response.products()).isEmpty();
	}
}