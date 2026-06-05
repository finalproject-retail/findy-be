package com.princesses7.findy.analytics.inventory.dto.query;

import java.time.LocalDate;

public record StockoutDailyQueryResult(
	LocalDate analysisDate,
	Long stockoutOccurrenceCount,
	Long stockoutProductCount
) {
}