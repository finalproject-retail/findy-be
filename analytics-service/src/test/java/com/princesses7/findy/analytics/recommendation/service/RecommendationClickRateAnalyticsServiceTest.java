package com.princesses7.findy.analytics.recommendation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationClickRateResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationClickRateAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateSummaryProjection;

@ExtendWith(MockitoExtension.class)
class RecommendationClickRateAnalyticsServiceTest {

	@Mock
	private RecommendationClickRateAnalyticsRepository recommendationClickRateAnalyticsRepository;

	private RecommendationClickRateAnalyticsService recommendationClickRateAnalyticsService;

	@BeforeEach
	void setUp() {
		recommendationClickRateAnalyticsService = new RecommendationClickRateAnalyticsService(
			recommendationClickRateAnalyticsRepository,
			new AnalyticsPeriodResolver()
		);
	}

	@Test
	@DisplayName("추천 클릭률 분석 데이터를 조회한다")
	void getClickRateAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 3);

		given(recommendationClickRateAnalyticsRepository.findSummary(
			"PERSONALIZED",
			fromDate,
			toDate
		)).willReturn(summary(100L, 25L));

		given(recommendationClickRateAnalyticsRepository.findDailyTrend(
			"PERSONALIZED",
			fromDate,
			toDate
		)).willReturn(List.of(
			daily(LocalDate.of(2026, 5, 1), 40L, 10L),
			daily(LocalDate.of(2026, 5, 2), 60L, 15L)
		));

		given(recommendationClickRateAnalyticsRepository.findTopProducts(
			eq("PERSONALIZED"),
			eq(fromDate),
			eq(toDate),
			any(Pageable.class)
		)).willReturn(List.of(
			product("PERSONALIZED", 10001L, "시드_신라면", 70L, 21L),
			product("PERSONALIZED", 10002L, "시드_햇반", 30L, 4L)
		));

		RecommendationClickRateResponse response =
			recommendationClickRateAnalyticsService.getClickRateAnalytics(
				fromDate,
				toDate,
				"personalized",
				10
			);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.recommendationType()).isEqualTo("PERSONALIZED");
		assertThat(response.impressionCount()).isEqualTo(100L);
		assertThat(response.clickCount()).isEqualTo(25L);
		assertThat(response.clickRate()).isEqualByComparingTo("25.00");
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).clickRate()).isEqualByComparingTo("30.00");
	}

	@Test
	@DisplayName("추천 노출 수가 0이면 클릭률은 0으로 반환한다")
	void getZeroClickRateWhenImpressionCountIsZero() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 1);

		given(recommendationClickRateAnalyticsRepository.findSummary(
			null,
			fromDate,
			toDate
		)).willReturn(summary(0L, 0L));

		given(recommendationClickRateAnalyticsRepository.findDailyTrend(
			null,
			fromDate,
			toDate
		)).willReturn(List.of());

		given(recommendationClickRateAnalyticsRepository.findTopProducts(
			isNull(),
			eq(fromDate),
			eq(toDate),
			any(Pageable.class)
		)).willReturn(List.of());

		RecommendationClickRateResponse response =
			recommendationClickRateAnalyticsService.getClickRateAnalytics(
				fromDate,
				toDate,
				null,
				10
			);

		assertThat(response.impressionCount()).isZero();
		assertThat(response.clickCount()).isZero();
		assertThat(response.clickRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).isEmpty();
		assertThat(response.products()).isEmpty();
	}

	private RecommendationClickRateSummaryProjection summary(
		Long impressionCount,
		Long clickCount
	) {
		return new RecommendationClickRateSummaryProjection() {
			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getClickCount() {
				return clickCount;
			}
		};
	}

	private RecommendationClickRateDailyProjection daily(
		LocalDate analysisDate,
		Long impressionCount,
		Long clickCount
	) {
		return new RecommendationClickRateDailyProjection() {
			@Override
			public LocalDate getAnalysisDate() {
				return analysisDate;
			}

			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getClickCount() {
				return clickCount;
			}
		};
	}

	private RecommendationClickRateProductProjection product(
		String recommendationType,
		Long productId,
		String productName,
		Long impressionCount,
		Long clickCount
	) {
		return new RecommendationClickRateProductProjection() {
			@Override
			public String getRecommendationType() {
				return recommendationType;
			}

			@Override
			public Long getProductId() {
				return productId;
			}

			@Override
			public String getProductName() {
				return productName;
			}

			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getClickCount() {
				return clickCount;
			}
		};
	}
}