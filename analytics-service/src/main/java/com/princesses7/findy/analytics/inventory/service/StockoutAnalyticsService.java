package com.princesses7.findy.analytics.inventory.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutProductQueryResult;
import com.princesses7.findy.analytics.inventory.dto.query.StockoutSummaryQueryResult;
import com.princesses7.findy.analytics.inventory.dto.response.StockoutAnalyticsResponse;
import com.princesses7.findy.analytics.inventory.dto.response.StockoutDailyResponse;
import com.princesses7.findy.analytics.inventory.dto.response.StockoutProductResponse;
import com.princesses7.findy.analytics.inventory.repository.StockoutAnalyticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockoutAnalyticsService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final StockoutAnalyticsRepository stockoutAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public StockoutAnalyticsResponse getStockoutAnalytics(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Long categoryId,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);

		StockoutSummaryQueryResult summary = stockoutAnalyticsRepository.findSummary(
			periodRange,
			storeId,
			categoryId
		);

		List<StockoutDailyResponse> dailyTrends = stockoutAnalyticsRepository.findDailyTrends(
				periodRange,
				storeId,
				categoryId
			)
			.stream()
			.map(StockoutDailyResponse::from)
			.toList();

		List<StockoutProductResponse> products = toProductResponses(
			stockoutAnalyticsRepository.findStockoutProducts(
				periodRange,
				storeId,
				categoryId,
				resolvedLimit
			)
		);

		Long totalInventoryProductCount = getTotalInventoryProductCount(summary);
		Long stockoutProductCount = getStockoutProductCount(summary);

		return new StockoutAnalyticsResponse(
			PeriodResponse.from(periodRange),
			storeId,
			categoryId,
			totalInventoryProductCount,
			stockoutProductCount,
			getStockoutOccurrenceCount(summary),
			getLowStockProductCount(summary),
			calculateRate(stockoutProductCount, totalInventoryProductCount),
			resolvedLimit,
			dailyTrends,
			products
		);
	}

	private List<StockoutProductResponse> toProductResponses(List<StockoutProductQueryResult> products) {
		return IntStream.range(0, products.size())
			.mapToObj(index -> StockoutProductResponse.of(index + 1, products.get(index)))
			.toList();
	}

	private BigDecimal calculateRate(Long numerator, Long denominator) {
		if (denominator == null || denominator == 0 || numerator == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(numerator)
			.multiply(HUNDRED)
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private Long getTotalInventoryProductCount(StockoutSummaryQueryResult summary) {
		return summary == null || summary.totalInventoryProductCount() == null
			? 0L
			: summary.totalInventoryProductCount();
	}

	private Long getStockoutProductCount(StockoutSummaryQueryResult summary) {
		return summary == null || summary.stockoutProductCount() == null
			? 0L
			: summary.stockoutProductCount();
	}

	private Long getStockoutOccurrenceCount(StockoutSummaryQueryResult summary) {
		return summary == null || summary.stockoutOccurrenceCount() == null
			? 0L
			: summary.stockoutOccurrenceCount();
	}

	private Long getLowStockProductCount(StockoutSummaryQueryResult summary) {
		return summary == null || summary.lowStockProductCount() == null
			? 0L
			: summary.lowStockProductCount();
	}
}