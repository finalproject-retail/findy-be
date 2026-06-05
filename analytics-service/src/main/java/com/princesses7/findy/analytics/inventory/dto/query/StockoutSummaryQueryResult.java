package com.princesses7.findy.analytics.inventory.dto.query;

public record StockoutSummaryQueryResult(
	Long totalInventoryProductCount,
	Long stockoutProductCount,
	Long stockoutOccurrenceCount,
	Long lowStockProductCount
) {
}