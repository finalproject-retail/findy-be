package com.princesses7.findy.analytics.recommendation.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationPurchaseConversionResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationPurchaseConversionAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionSummaryProjection;

@ExtendWith(MockitoExtension.class)
class RecommendationPurchaseConversionAnalyticsServiceTest {

	@Mock
	private RecommendationPurchaseConversionAnalyticsRepository recommendationPurchaseConversionAnalyticsRepository;

	private RecommendationPurchaseConversionAnalyticsService recommendationPurchaseConversionAnalyticsService;

	@BeforeEach
	void setUp() {
		recommendationPurchaseConversionAnalyticsService = new RecommendationPurchaseConversionAnalyticsService(
			recommendationPurchaseConversionAnalyticsRepository,
			new AnalyticsPeriodResolver()
		);
	}

	@Test
	@DisplayName("추천 후 구매 전환율 분석 데이터를 조회한다")
	void getPurchaseConversionAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 2);
		LocalDateTime fromDateTime = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime toDateTime = LocalDateTime.of(2026, 5, 3, 0, 0);

		given(recommendationPurchaseConversionAnalyticsRepository.findSummary(
			"PERSONALIZED",
			null,
			fromDateTime,
			toDateTime
		)).willReturn(summary(5L, 4L, 3L));

		given(recommendationPurchaseConversionAnalyticsRepository.findDailyTrends(
			"PERSONALIZED",
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of(
			daily(LocalDate.of(2026, 5, 1), 3L, 2L, 1L),
			daily(LocalDate.of(2026, 5, 2), 2L, 2L, 2L)
		));

		given(recommendationPurchaseConversionAnalyticsRepository.findTopProducts(
			"PERSONALIZED",
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of(
			product("PERSONALIZED", 10001L, "시드_신라면", 3L, 3L, 2L),
			product("PERSONALIZED", 10002L, "시드_햇반", 2L, 1L, 1L)
		));

		RecommendationPurchaseConversionResponse response =
			recommendationPurchaseConversionAnalyticsService.getPurchaseConversionAnalytics(
				fromDate,
				toDate,
				"personalized",
				null,
				10
			);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.recommendationType()).isEqualTo("PERSONALIZED");
		assertThat(response.productId()).isNull();
		assertThat(response.impressionCount()).isEqualTo(5L);
		assertThat(response.clickCount()).isEqualTo(4L);
		assertThat(response.purchaseCount()).isEqualTo(3L);
		assertThat(response.purchaseConversionRate()).isEqualByComparingTo("60.00");
		assertThat(response.clickToPurchaseRate()).isEqualByComparingTo("75.00");
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).purchaseConversionRate()).isEqualByComparingTo("66.67");
	}

	@Test
	@DisplayName("추천 노출 수와 클릭 수가 0이면 전환율은 0으로 반환한다")
	void getZeroConversionRateWhenImpressionAndClickCountAreZero() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 1);
		LocalDateTime fromDateTime = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime toDateTime = LocalDateTime.of(2026, 5, 2, 0, 0);

		given(recommendationPurchaseConversionAnalyticsRepository.findSummary(
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(summary(0L, 0L, 0L));

		given(recommendationPurchaseConversionAnalyticsRepository.findDailyTrends(
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of());

		given(recommendationPurchaseConversionAnalyticsRepository.findTopProducts(
			null,
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of());

		RecommendationPurchaseConversionResponse response =
			recommendationPurchaseConversionAnalyticsService.getPurchaseConversionAnalytics(
				fromDate,
				toDate,
				null,
				null,
				10
			);

		assertThat(response.impressionCount()).isZero();
		assertThat(response.clickCount()).isZero();
		assertThat(response.purchaseCount()).isZero();
		assertThat(response.purchaseConversionRate()).isEqualByComparingTo("0.00");
		assertThat(response.clickToPurchaseRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).isEmpty();
		assertThat(response.products()).isEmpty();
	}

	private RecommendationPurchaseConversionSummaryProjection summary(
		Long impressionCount,
		Long clickCount,
		Long purchaseCount
	) {
		return new RecommendationPurchaseConversionSummaryProjection() {
			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getClickCount() {
				return clickCount;
			}

			@Override
			public Long getPurchaseCount() {
				return purchaseCount;
			}
		};
	}

	private RecommendationPurchaseConversionDailyProjection daily(
		LocalDate analysisDate,
		Long impressionCount,
		Long clickCount,
		Long purchaseCount
	) {
		return new RecommendationPurchaseConversionDailyProjection() {
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

			@Override
			public Long getPurchaseCount() {
				return purchaseCount;
			}
		};
	}

	private RecommendationPurchaseConversionProductProjection product(
		String recommendationType,
		Long productId,
		String productName,
		Long impressionCount,
		Long clickCount,
		Long purchaseCount
	) {
		return new RecommendationPurchaseConversionProductProjection() {
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

			@Override
			public Long getPurchaseCount() {
				return purchaseCount;
			}
		};
	}
}