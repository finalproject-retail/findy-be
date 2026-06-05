package com.princesses7.findy.analytics.inventory.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.analytics.inventory.dto.query.StockoutProductQueryResult;

public record StockoutProductResponse(
	int rankNo,
	Long productId,
	String productName,
	String brandName,
	Long categoryId,
	String categoryName,
	Long storeId,
	Integer stockQuantity,
	String stockStatus,
	LocalDateTime stockoutOccurredAt
) {

	public static StockoutProductResponse of(int rankNo, StockoutProductQueryResult result) {
		return new StockoutProductResponse(
			rankNo,
			result.productId(),
			result.productName(),
			result.brandName(),
			result.categoryId(),
			result.categoryName(),
			result.storeId(),
			result.stockQuantity(),
			result.stockStatus(),
			result.stockoutOccurredAt()
		);
	}
}