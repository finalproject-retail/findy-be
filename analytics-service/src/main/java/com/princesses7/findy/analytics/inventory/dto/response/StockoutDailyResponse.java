package com.princesses7.findy.analytics.inventory.dto.response;

import java.time.LocalDate;

import com.princesses7.findy.analytics.inventory.dto.query.StockoutDailyQueryResult;

public record StockoutDailyResponse(
	LocalDate analysisDate,
	Long stockoutOccurrenceCount,
	Long stockoutProductCount
) {

	public static StockoutDailyResponse from(StockoutDailyQueryResult result) {
		return new StockoutDailyResponse(
			result.analysisDate(),
			result.stockoutOccurrenceCount(),
			result.stockoutProductCount()
		);
	}
}