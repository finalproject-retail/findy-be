package com.princesses7.findy.analytics.inventory.dto.query;

import java.time.LocalDateTime;

public record StockoutProductQueryResult(
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
}