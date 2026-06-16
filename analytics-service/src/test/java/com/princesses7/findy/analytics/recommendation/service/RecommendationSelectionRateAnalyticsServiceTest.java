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
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationSelectionRateResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationSelectionRateAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateDailyProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateProductProjection;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateSummaryProjection;

@ExtendWith(MockitoExtension.class)
class RecommendationSelectionRateAnalyticsServiceTest {

	@Mock
	private RecommendationSelectionRateAnalyticsRepository recommendationSelectionRateAnalyticsRepository;

	private RecommendationSelectionRateAnalyticsService recommendationSelectionRateAnalyticsService;

	@BeforeEach
	void setUp() {
		recommendationSelectionRateAnalyticsService = new RecommendationSelectionRateAnalyticsService(
			recommendationSelectionRateAnalyticsRepository,
			new AnalyticsPeriodResolver()
		);
	}

	@Test
	@DisplayName("대체상품 및 행사상품 선택률 분석 데이터를 조회한다")
	void getSelectionRateAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 2);
		LocalDateTime fromDateTime = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime toDateTime = LocalDateTime.of(2026, 5, 3, 0, 0);

		given(recommendationSelectionRateAnalyticsRepository.findSummary(
			"SUBSTITUTE",
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(summary(10L, 4L));

		given(recommendationSelectionRateAnalyticsRepository.findDailyTrends(
			"SUBSTITUTE",
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of(
			daily(LocalDate.of(2026, 5, 1), 4L, 1L),
			daily(LocalDate.of(2026, 5, 2), 6L, 3L)
		));

		given(recommendationSelectionRateAnalyticsRepository.findTopProducts(
			"SUBSTITUTE",
			null,
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of(
			product("SUBSTITUTE", 10001L, 10002L, "대체_사리곰탕", 6L, 3L),
			product("SUBSTITUTE", 10003L, 10004L, "대체_햇반", 4L, 1L)
		));

		RecommendationSelectionRateResponse response =
			recommendationSelectionRateAnalyticsService.getSelectionRateAnalytics(
				fromDate,
				toDate,
				"substitute",
				null,
				null,
				10
			);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.recommendationType()).isEqualTo("SUBSTITUTE");
		assertThat(response.productId()).isNull();
		assertThat(response.sourceProductId()).isNull();
		assertThat(response.impressionCount()).isEqualTo(10L);
		assertThat(response.selectionCount()).isEqualTo(4L);
		assertThat(response.selectionRate()).isEqualByComparingTo("40.00");
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.dailyTrends().get(0).selectionRate()).isEqualByComparingTo("25.00");
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).sourceProductId()).isEqualTo(10001L);
		assertThat(response.products().get(0).productId()).isEqualTo(10002L);
		assertThat(response.products().get(0).selectionRate()).isEqualByComparingTo("50.00");
	}

	@Test
	@DisplayName("추천 노출 수가 0이면 선택률은 0으로 반환한다")
	void getZeroSelectionRateWhenImpressionCountIsZero() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 1);
		LocalDateTime fromDateTime = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime toDateTime = LocalDateTime.of(2026, 5, 2, 0, 0);

		given(recommendationSelectionRateAnalyticsRepository.findSummary(
			null,
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(summary(0L, 0L));

		given(recommendationSelectionRateAnalyticsRepository.findDailyTrends(
			null,
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of());

		given(recommendationSelectionRateAnalyticsRepository.findTopProducts(
			null,
			null,
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of());

		RecommendationSelectionRateResponse response =
			recommendationSelectionRateAnalyticsService.getSelectionRateAnalytics(
				fromDate,
				toDate,
				null,
				null,
				null,
				10
			);

		assertThat(response.impressionCount()).isZero();
		assertThat(response.selectionCount()).isZero();
		assertThat(response.selectionRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).isEmpty();
		assertThat(response.products()).isEmpty();
	}

	private RecommendationSelectionRateSummaryProjection summary(
		Long impressionCount,
		Long selectionCount
	) {
		return new RecommendationSelectionRateSummaryProjection() {
			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getSelectionCount() {
				return selectionCount;
			}

				@Override
				public Long getPurchaseCount() {
					return 0L;
				}
		};
	}

	private RecommendationSelectionRateDailyProjection daily(
		LocalDate analysisDate,
		Long impressionCount,
		Long selectionCount
	) {
		return new RecommendationSelectionRateDailyProjection() {
			@Override
			public LocalDate getAnalysisDate() {
				return analysisDate;
			}

			@Override
			public Long getImpressionCount() {
				return impressionCount;
			}

			@Override
			public Long getSelectionCount() {
				return selectionCount;
			}

				@Override
				public Long getPurchaseCount() {
					return 0L;
				}
		};
	}

	private RecommendationSelectionRateProductProjection product(
		String recommendationType,
		Long sourceProductId,
		Long productId,
		String productName,
		Long impressionCount,
		Long selectionCount
	) {
		return new RecommendationSelectionRateProductProjection() {
			@Override
			public String getRecommendationType() {
				return recommendationType;
			}

			@Override
			public Long getSourceProductId() {
				return sourceProductId;
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
			public Long getSelectionCount() {
				return selectionCount;
			}

				@Override
				public Long getPurchaseCount() {
					return 0L;
				}
		};
	}
}