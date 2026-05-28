package com.princesses7.findy.analytics.product.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record ProductPerformanceSummaryResponse(
	PeriodResponse period,
	Long storeId,
	Long totalProductCount,
	Long viewedProductCount,
	Long orderedProductCount,
	Long totalViewCount,
	Long totalOrderCount,
	Long totalOrderQuantity,
	Long totalSalesAmount,
	BigDecimal purchaseConversionRate,
	BigDecimal averageSalesAmountPerOrder,
	Integer limit,
	List<ProductPerformanceItemResponse> products
) {
}