package com.princesses7.findy.analytics.product.service;

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
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceProductQueryResult;
import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceSummaryQueryResult;
import com.princesses7.findy.analytics.product.dto.response.ProductPerformanceItemResponse;
import com.princesses7.findy.analytics.product.dto.response.ProductPerformanceSummaryResponse;
import com.princesses7.findy.analytics.product.repository.ProductPerformanceSummaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductPerformanceSummaryService {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

	private final ProductPerformanceSummaryRepository productPerformanceSummaryRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public ProductPerformanceSummaryResponse getProductPerformanceSummary(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = analyticsPeriodResolver.resolveLimit(limit);

		ProductPerformanceSummaryQueryResult summary = productPerformanceSummaryRepository.findSummary(
			periodRange,
			storeId
		);

		List<ProductPerformanceProductQueryResult> topProducts = productPerformanceSummaryRepository.findTopProducts(
			periodRange,
			storeId,
			resolvedLimit
		);

		List<ProductPerformanceItemResponse> productResponses = toProductResponses(
			topProducts,
			getTotalSalesAmount(summary)
		);

		return new ProductPerformanceSummaryResponse(
			PeriodResponse.from(periodRange),
			storeId,
			getTotalProductCount(summary),
			getViewedProductCount(summary),
			getOrderedProductCount(summary),
			getTotalViewCount(summary),
			getTotalOrderCount(summary),
			getTotalOrderQuantity(summary),
			getTotalSalesAmount(summary),
			calculateRate(getTotalOrderQuantity(summary), getTotalViewCount(summary)),
			calculateAverageSalesAmount(getTotalSalesAmount(summary), getTotalOrderCount(summary)),
			resolvedLimit,
			productResponses
		);
	}

	private List<ProductPerformanceItemResponse> toProductResponses(
		List<ProductPerformanceProductQueryResult> topProducts,
		Long totalSalesAmount
	) {
		return IntStream.range(0, topProducts.size())
			.mapToObj(index -> ProductPerformanceItemResponse.of(
				index + 1,
				topProducts.get(index),
				calculateRate(topProducts.get(index).orderQuantity(), topProducts.get(index).viewCount()),
				calculateRate(topProducts.get(index).salesAmount(), totalSalesAmount)
			))
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

	private BigDecimal calculateAverageSalesAmount(Long totalSalesAmount, Long totalOrderCount) {
		if (totalOrderCount == null || totalOrderCount == 0 || totalSalesAmount == null) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(totalSalesAmount)
			.divide(BigDecimal.valueOf(totalOrderCount), 2, RoundingMode.HALF_UP);
	}

	private Long getTotalProductCount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.totalProductCount() == null ? 0L : summary.totalProductCount();
	}

	private Long getViewedProductCount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.viewedProductCount() == null ? 0L : summary.viewedProductCount();
	}

	private Long getOrderedProductCount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.orderedProductCount() == null ? 0L : summary.orderedProductCount();
	}

	private Long getTotalViewCount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.totalViewCount() == null ? 0L : summary.totalViewCount();
	}

	private Long getTotalOrderCount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.totalOrderCount() == null ? 0L : summary.totalOrderCount();
	}

	private Long getTotalOrderQuantity(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.totalOrderQuantity() == null ? 0L : summary.totalOrderQuantity();
	}

	private Long getTotalSalesAmount(ProductPerformanceSummaryQueryResult summary) {
		return summary == null || summary.totalSalesAmount() == null ? 0L : summary.totalSalesAmount();
	}
}