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
	@DisplayName("대체상품 선택률 분석 데이터를 조회한다")
	void getSubstituteSelectionRateAnalytics() {
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
		)).willReturn(summary(10L, 4L, 0L));

		given(recommendationSelectionRateAnalyticsRepository.findDailyTrends(
			"SUBSTITUTE",
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of(
			daily(LocalDate.of(2026, 5, 1), 4L, 1L, 0L),
			daily(LocalDate.of(2026, 5, 2), 6L, 3L, 0L)
		));

		given(recommendationSelectionRateAnalyticsRepository.findTopProducts(
			"SUBSTITUTE",
			null,
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of(
			product("SUBSTITUTE", 10001L, 10002L, "대체 사리곰탕", 6L, 3L, 0L),
			product("SUBSTITUTE", 10003L, 10004L, "대체 햇반", 4L, 1L, 0L)
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
		assertThat(response.purchaseCount()).isZero();
		assertThat(response.selectionRate()).isEqualByComparingTo("40.00");
		assertThat(response.conversionRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.dailyTrends().get(0).selectionRate()).isEqualByComparingTo("25.00");
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).sourceProductId()).isEqualTo(10001L);
		assertThat(response.products().get(0).productId()).isEqualTo(10002L);
		assertThat(response.products().get(0).selectionRate()).isEqualByComparingTo("50.00");
		assertThat(response.products().get(0).promotionName()).isNull();
		assertThat(response.products().get(0).promotionType()).isNull();
		assertThat(response.products().get(0).promotionLabel()).isEqualTo("행사");
	}

	@Test
	@DisplayName("행사상품 선택률 분석 데이터는 실제 행사 상품 조회 쿼리로 조회한다")
	void getPromotionSelectionRateAnalytics() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 2);
		LocalDateTime fromDateTime = LocalDateTime.of(2026, 5, 1, 0, 0);
		LocalDateTime toDateTime = LocalDateTime.of(2026, 5, 3, 0, 0);

		given(recommendationSelectionRateAnalyticsRepository.findSummary(
			"PROMOTION",
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(summary(20L, 8L, 3L));

		given(recommendationSelectionRateAnalyticsRepository.findDailyTrends(
			"PROMOTION",
			null,
			null,
			fromDateTime,
			toDateTime
		)).willReturn(List.of(
			daily(LocalDate.of(2026, 5, 1), 10L, 3L, 1L),
			daily(LocalDate.of(2026, 5, 2), 10L, 5L, 2L)
		));

		given(recommendationSelectionRateAnalyticsRepository.findTopPromotionProducts(
			null,
			null,
			fromDateTime,
			toDateTime,
			10
		)).willReturn(List.of(
			promotionProduct(
				20001L,
				10001L,
				"농심 신라면",
				"라면 1+1 행사",
				"ONE_PLUS_ONE",
				"1+1",
				12L,
				6L,
				2L
			),
			promotionProduct(
				20002L,
				10003L,
				"햇반 백미밥",
				"즉석밥 할인 행사",
				"DISCOUNT",
				"할인 행사",
				8L,
				2L,
				1L
			)
		));

		RecommendationSelectionRateResponse response =
			recommendationSelectionRateAnalyticsService.getSelectionRateAnalytics(
				fromDate,
				toDate,
				"promotion",
				null,
				null,
				10
			);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.recommendationType()).isEqualTo("PROMOTION");
		assertThat(response.impressionCount()).isEqualTo(20L);
		assertThat(response.selectionCount()).isEqualTo(8L);
		assertThat(response.purchaseCount()).isEqualTo(3L);
		assertThat(response.selectionRate()).isEqualByComparingTo("40.00");
		assertThat(response.conversionRate()).isEqualByComparingTo("15.00");
		assertThat(response.dailyTrends()).hasSize(2);
		assertThat(response.products()).hasSize(2);
		assertThat(response.products().get(0).sourceProductId()).isEqualTo(20001L);
		assertThat(response.products().get(0).productId()).isEqualTo(10001L);
		assertThat(response.products().get(0).productName()).isEqualTo("농심 신라면");
		assertThat(response.products().get(0).promotionName()).isEqualTo("라면 1+1 행사");
		assertThat(response.products().get(0).promotionType()).isEqualTo("ONE_PLUS_ONE");
		assertThat(response.products().get(0).promotionLabel()).isEqualTo("1+1");
		assertThat(response.products().get(0).selectionRate()).isEqualByComparingTo("50.00");
		assertThat(response.products().get(0).conversionRate()).isEqualByComparingTo("16.67");
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
		)).willReturn(summary(0L, 0L, 0L));

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
		assertThat(response.purchaseCount()).isZero();
		assertThat(response.selectionRate()).isEqualByComparingTo("0.00");
		assertThat(response.conversionRate()).isEqualByComparingTo("0.00");
		assertThat(response.dailyTrends()).isEmpty();
		assertThat(response.products()).isEmpty();
	}

	private RecommendationSelectionRateSummaryProjection summary(
		Long impressionCount,
		Long selectionCount,
		Long purchaseCount
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
				return purchaseCount;
			}
		};
	}

	private RecommendationSelectionRateDailyProjection daily(
		LocalDate analysisDate,
		Long impressionCount,
		Long selectionCount,
		Long purchaseCount
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
				return purchaseCount;
			}
		};
	}

	private RecommendationSelectionRateProductProjection product(
		String recommendationType,
		Long sourceProductId,
		Long productId,
		String productName,
		Long impressionCount,
		Long selectionCount,
		Long purchaseCount
	) {
		return product(
			recommendationType,
			sourceProductId,
			productId,
			productName,
			null,
			null,
			null,
			impressionCount,
			selectionCount,
			purchaseCount
		);
	}

	private RecommendationSelectionRateProductProjection promotionProduct(
		Long promotionId,
		Long productId,
		String productName,
		String promotionName,
		String promotionType,
		String promotionLabel,
		Long impressionCount,
		Long selectionCount,
		Long purchaseCount
	) {
		return product(
			"PROMOTION",
			promotionId,
			productId,
			productName,
			promotionName,
			promotionType,
			promotionLabel,
			impressionCount,
			selectionCount,
			purchaseCount
		);
	}

	private RecommendationSelectionRateProductProjection product(
		String recommendationType,
		Long sourceProductId,
		Long productId,
		String productName,
		String promotionName,
		String promotionType,
		String promotionLabel,
		Long impressionCount,
		Long selectionCount,
		Long purchaseCount
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
			public String getPromotionName() {
				return promotionName;
			}

			@Override
			public String getPromotionType() {
				return promotionType;
			}

			@Override
			public String getPromotionLabel() {
				return promotionLabel;
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
				return purchaseCount;
			}
		};
	}
}