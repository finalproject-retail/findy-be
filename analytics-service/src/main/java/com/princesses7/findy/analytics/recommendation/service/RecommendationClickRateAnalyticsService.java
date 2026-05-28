package com.princesses7.findy.analytics.recommendation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationClickRateDailyResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationClickRateProductResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationClickRateResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationClickRateAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationClickRateSummaryProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationClickRateAnalyticsService {

	private final RecommendationClickRateAnalyticsRepository recommendationClickRateAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public RecommendationClickRateResponse getClickRateAnalytics(
		LocalDate fromDate,
		LocalDate toDate,
		String recommendationType,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);
		String normalizedRecommendationType = normalizeRecommendationType(recommendationType);

		RecommendationClickRateSummaryProjection summary =
			recommendationClickRateAnalyticsRepository.findSummary(
				normalizedRecommendationType,
				periodRange.fromDate(),
				periodRange.toDate()
			);

		List<RecommendationClickRateDailyResponse> dailyTrends =
			recommendationClickRateAnalyticsRepository.findDailyTrend(
					normalizedRecommendationType,
					periodRange.fromDate(),
					periodRange.toDate()
				)
				.stream()
				.map(RecommendationClickRateDailyResponse::from)
				.toList();

		List<RecommendationClickRateProductResponse> products =
			recommendationClickRateAnalyticsRepository.findTopProducts(
					normalizedRecommendationType,
					periodRange.fromDate(),
					periodRange.toDate(),
					PageRequest.of(0, resolvedLimit)
				)
				.stream()
				.map(RecommendationClickRateProductResponse::from)
				.toList();

		return RecommendationClickRateResponse.of(
			PeriodResponse.from(periodRange),
			normalizedRecommendationType,
			getImpressionCount(summary),
			getClickCount(summary),
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

	private long getImpressionCount(RecommendationClickRateSummaryProjection summary) {
		if (summary == null || summary.getImpressionCount() == null) {
			return 0L;
		}

		return summary.getImpressionCount();
	}

	private long getClickCount(RecommendationClickRateSummaryProjection summary) {
		if (summary == null || summary.getClickCount() == null) {
			return 0L;
		}

		return summary.getClickCount();
	}
}