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
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationPurchaseConversionDailyResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationPurchaseConversionProductResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationPurchaseConversionResponse;
import com.princesses7.findy.analytics.recommendation.repository.RecommendationPurchaseConversionAnalyticsRepository;
import com.princesses7.findy.analytics.recommendation.repository.projection.RecommendationPurchaseConversionSummaryProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationPurchaseConversionAnalyticsService {

	private final RecommendationPurchaseConversionAnalyticsRepository recommendationPurchaseConversionAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public RecommendationPurchaseConversionResponse getPurchaseConversionAnalytics(
		LocalDate startDate,
		LocalDate endDate,
		String recommendationType,
		Long productId,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(startDate, endDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);
		String normalizedRecommendationType = normalizeRecommendationType(recommendationType);

		LocalDateTime startDateTime = periodRange.startDate().atStartOfDay();
		LocalDateTime endDateTime = periodRange.endDate().plusDays(1).atStartOfDay();

		RecommendationPurchaseConversionSummaryProjection summary =
			recommendationPurchaseConversionAnalyticsRepository.findSummary(
				normalizedRecommendationType,
				productId,
				startDateTime,
				endDateTime
			);

		List<RecommendationPurchaseConversionDailyResponse> dailyTrends =
			recommendationPurchaseConversionAnalyticsRepository.findDailyTrends(
					normalizedRecommendationType,
					productId,
					startDateTime,
					endDateTime
				)
				.stream()
				.map(RecommendationPurchaseConversionDailyResponse::from)
				.toList();

		List<RecommendationPurchaseConversionProductResponse> products =
			recommendationPurchaseConversionAnalyticsRepository.findTopProducts(
					normalizedRecommendationType,
					productId,
					startDateTime,
					endDateTime,
					resolvedLimit
				)
				.stream()
				.map(RecommendationPurchaseConversionProductResponse::from)
				.toList();

		return RecommendationPurchaseConversionResponse.of(
			PeriodResponse.from(periodRange),
			normalizedRecommendationType,
			productId,
			getImpressionCount(summary),
			getClickCount(summary),
			getPurchaseCount(summary),
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

	private long getImpressionCount(RecommendationPurchaseConversionSummaryProjection summary) {
		if (summary == null || summary.getImpressionCount() == null) {
			return 0L;
		}

		return summary.getImpressionCount();
	}

	private long getClickCount(RecommendationPurchaseConversionSummaryProjection summary) {
		if (summary == null || summary.getClickCount() == null) {
			return 0L;
		}

		return summary.getClickCount();
	}

	private long getPurchaseCount(RecommendationPurchaseConversionSummaryProjection summary) {
		if (summary == null || summary.getPurchaseCount() == null) {
			return 0L;
		}

		return summary.getPurchaseCount();
	}
}