package com.princesses7.findy.analytics.inventory.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;

public record StockoutAnalyticsResponse(
	PeriodResponse period,
	Long storeId,
	Long categoryId,
	Long totalInventoryProductCount,
	Long stockoutProductCount,
	Long stockoutOccurrenceCount,
	Long lowStockProductCount,
	BigDecimal stockoutRate,
	Integer limit,
	List<StockoutDailyResponse> dailyTrends,
	List<StockoutProductResponse> products
) {
}