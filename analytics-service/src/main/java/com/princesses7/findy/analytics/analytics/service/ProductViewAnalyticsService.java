package com.princesses7.findy.analytics.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.query.ProductViewRankingQueryResult;
import com.princesses7.findy.analytics.analytics.dto.response.ProductViewAnalyticsResponse;
import com.princesses7.findy.analytics.analytics.dto.response.ProductViewRankingItemResponse;
import com.princesses7.findy.analytics.analytics.repository.ProductViewAnalyticsRepository;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductViewAnalyticsService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final ProductViewAnalyticsRepository productViewAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public ProductViewAnalyticsResponse getProductViewAnalytics(
		LocalDate fromDate,
		LocalDate toDate,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);

		Long totalViewCount = productViewAnalyticsRepository.countTotalViews(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		);

		Long viewedProductCount = productViewAnalyticsRepository.countViewedProducts(
			periodRange.fromAt(),
			periodRange.toExclusiveAt()
		);

		List<ProductViewRankingQueryResult> rankings = productViewAnalyticsRepository.findProductViewRankings(
			periodRange.fromAt(),
			periodRange.toExclusiveAt(),
			resolvedLimit
		);

		List<ProductViewRankingItemResponse> products = toRankingResponses(rankings, totalViewCount);

		return new ProductViewAnalyticsResponse(
			periodRange.fromDate(),
			periodRange.toDate(),
			totalViewCount,
			viewedProductCount,
			resolvedLimit,
			products
		);
	}

	private List<ProductViewRankingItemResponse> toRankingResponses(
		List<ProductViewRankingQueryResult> rankings,
		Long totalViewCount
	) {
		return IntStream.range(0, rankings.size())
			.mapToObj(index -> ProductViewRankingItemResponse.of(
				index + 1,
				rankings.get(index),
				calculateViewRate(rankings.get(index).viewCount(), totalViewCount)
			))
			.toList();
	}

	private BigDecimal calculateViewRate(Long viewCount, Long totalViewCount) {
		if (totalViewCount == null || totalViewCount == 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(viewCount)
			.multiply(HUNDRED)
			.divide(BigDecimal.valueOf(totalViewCount), 2, RoundingMode.HALF_UP);
	}
}