package com.princesses7.findy.analytics.recommendation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationSelectionRateDailyResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationSelectionRateProductResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationSelectionRateResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationSelectionRateAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationSelectionRateSummaryProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationSelectionRateAnalyticsService {

	private final RecommendationSelectionRateAnalyticsRepository recommendationSelectionRateAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public RecommendationSelectionRateResponse getSelectionRateAnalytics(
		LocalDate fromDate,
		LocalDate toDate,
		String recommendationType,
		Long productId,
		Long sourceProductId,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);
		String normalizedRecommendationType = normalizeRecommendationType(recommendationType);

		LocalDateTime fromDateTime = periodRange.fromDate().atStartOfDay();
		LocalDateTime toDateTime = periodRange.toDate().plusDays(1).atStartOfDay();

		RecommendationSelectionRateSummaryProjection summary =
			recommendationSelectionRateAnalyticsRepository.findSummary(
				normalizedRecommendationType,
				productId,
				sourceProductId,
				fromDateTime,
				toDateTime
			);

		List<RecommendationSelectionRateDailyResponse> dailyTrends =
			recommendationSelectionRateAnalyticsRepository.findDailyTrends(
					normalizedRecommendationType,
					productId,
					sourceProductId,
					fromDateTime,
					toDateTime
				)
				.stream()
				.map(RecommendationSelectionRateDailyResponse::from)
				.toList();

		List<RecommendationSelectionRateProductResponse> products =
			recommendationSelectionRateAnalyticsRepository.findTopProducts(
					normalizedRecommendationType,
					productId,
					sourceProductId,
					fromDateTime,
					toDateTime,
					resolvedLimit
				)
				.stream()
				.map(RecommendationSelectionRateProductResponse::from)
				.toList();

		return RecommendationSelectionRateResponse.of(
			PeriodResponse.from(periodRange),
			normalizedRecommendationType,
			productId,
			sourceProductId,
			getImpressionCount(summary),
			getSelectionCount(summary),
			dailyTrends,
			products
		);
	}

	private String normalizeRecommendationType(String recommendationType) {
		if (!StringUtils.hasText(recommendationType)) {
			return null;
		}

		return recommendationType.trim().toUpperCase(Locale.ROOT);
	}

	private long getImpressionCount(RecommendationSelectionRateSummaryProjection summary) {
		if (summary == null || summary.getImpressionCount() == null) {
			return 0L;
		}

		return summary.getImpressionCount();
	}

	private long getSelectionCount(RecommendationSelectionRateSummaryProjection summary) {
		if (summary == null || summary.getSelectionCount() == null) {
			return 0L;
		}

		return summary.getSelectionCount();
	}
}