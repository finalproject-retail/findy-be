package com.princesses7.findy.analytics.analytics.service;

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

import com.princesses7.findy.analytics.analytics.dto.query.ProductViewRankingQueryResult;
import com.princesses7.findy.analytics.analytics.dto.response.ProductViewAnalyticsResponse;
import com.princesses7.findy.analytics.analytics.repository.ProductViewAnalyticsRepository;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;

@ExtendWith(MockitoExtension.class)
class ProductViewAnalyticsServiceTest {

	@Mock
	private ProductViewAnalyticsRepository productViewAnalyticsRepository;

	@Mock
	private AnalyticsPeriodResolver analyticsPeriodResolver;

	@InjectMocks
	private ProductViewAnalyticsService productViewAnalyticsService;

	@Test
	@DisplayName("상품 조회수 분석 결과를 조회수 순위와 조회 비율로 반환한다")
	void getProductViewAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Integer limit = 10;

		PeriodRange periodRange = new PeriodRange(
			fromDate,
			toDate,
			fromDate.atStartOfDay(),
			toDate.plusDays(1).atStartOfDay()
		);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);

		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(10);

		given(productViewAnalyticsRepository.countTotalViews(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		)).willReturn(10L);

		given(productViewAnalyticsRepository.countViewedProducts(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		)).willReturn(2L);

		given(productViewAnalyticsRepository.findProductViewRankings(
			periodRange.fromAt(),
			periodRange.toExclusiveAt(),
			10
		)).willReturn(List.of(
			new ProductViewRankingQueryResult(1L, "시드_신라면", "농심", 1L, 7L),
			new ProductViewRankingQueryResult(2L, "시드_백미밥", "햇반", 3L, 3L)
		));

		ProductViewAnalyticsResponse response = productViewAnalyticsService.getProductViewAnalytics(
			fromDate,
			toDate,
			limit
		);

		assertThat(response.fromDate()).isEqualTo(fromDate);
		assertThat(response.toDate()).isEqualTo(toDate);
		assertThat(response.totalViewCount()).isEqualTo(10L);
		assertThat(response.viewedProductCount()).isEqualTo(2L);
		assertThat(response.limit()).isEqualTo(10);
		assertThat(response.products()).hasSize(2);

		assertThat(response.products().get(0).rankNo()).isEqualTo(1);
		assertThat(response.products().get(0).productId()).isEqualTo(1L);
		assertThat(response.products().get(0).viewCount()).isEqualTo(7L);
		assertThat(response.products().get(0).viewRate()).isEqualByComparingTo("70.00");

		assertThat(response.products().get(1).rankNo()).isEqualTo(2);
		assertThat(response.products().get(1).viewRate()).isEqualByComparingTo("30.00");
	}

	@Test
	@DisplayName("limit 값이 없으면 기본 limit으로 조회한다")
	void getProductViewAnalyticsWithDefaultLimit() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Integer limit = null;
		int defaultLimit = 10;

		PeriodRange periodRange = new PeriodRange(
			fromDate,
			toDate,
			fromDate.atStartOfDay(),
			toDate.plusDays(1).atStartOfDay()
		);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);

		given(analyticsPeriodResolver.resolveLimit(limit))
			.willReturn(defaultLimit);

		given(productViewAnalyticsRepository.countTotalViews(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		)).willReturn(0L);

		given(productViewAnalyticsRepository.countViewedProducts(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		)).willReturn(0L);

		given(productViewAnalyticsRepository.findProductViewRankings(
			periodRange.fromAt(),
			periodRange.toExclusiveAt(),
			defaultLimit
		)).willReturn(List.of());

		ProductViewAnalyticsResponse response = productViewAnalyticsService.getProductViewAnalytics(
			fromDate,
			toDate,
			limit
		);

		assertThat(response.limit()).isEqualTo(defaultLimit);
		assertThat(response.totalViewCount()).isEqualTo(0L);
		assertThat(response.viewedProductCount()).isEqualTo(0L);
		assertThat(response.products()).isEmpty();
	}
}