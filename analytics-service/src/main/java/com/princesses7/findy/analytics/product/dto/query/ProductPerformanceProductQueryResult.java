package com.princesses7.findy.analytics.product.dto.query;

public record ProductPerformanceProductQueryResult(
	Long productId,
	String productName,
	String brandName,
	Long categoryId,
	String categoryName,
	Long viewCount,
	Long orderCount,
	Long orderQuantity,
	Long salesAmount
) {
}