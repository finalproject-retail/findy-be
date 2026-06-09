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
		LocalDate startDate,
		LocalDate endDate,
		String recommendationType,
		Long productId,
		Long sourceProductId,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(startDate, endDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);
		String normalizedRecommendationType = normalizeRecommendationType(recommendationType);

		LocalDateTime startDateTime = periodRange.startDate().atStartOfDay();
		LocalDateTime endDateTime = periodRange.endDate().plusDays(1).atStartOfDay();

		RecommendationSelectionRateSummaryProjection summary =
			recommendationSelectionRateAnalyticsRepository.findSummary(
				normalizedRecommendationType,
				productId,
				sourceProductId,
				startDateTime,
				endDateTime
			);

		List<RecommendationSelectionRateDailyResponse> dailyTrends =
			recommendationSelectionRateAnalyticsRepository.findDailyTrends(
					normalizedRecommendationType,
					productId,
					sourceProductId,
					startDateTime,
					endDateTime
				)
				.stream()
				.map(RecommendationSelectionRateDailyResponse::from)
				.toList();

		List<RecommendationSelectionRateProductResponse> products =
			recommendationSelectionRateAnalyticsRepository.findTopProducts(
					normalizedRecommendationType,
					productId,
					sourceProductId,
					startDateTime,
					endDateTime,
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