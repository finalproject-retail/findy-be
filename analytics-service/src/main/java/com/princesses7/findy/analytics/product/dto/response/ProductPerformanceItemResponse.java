package com.princesses7.findy.analytics.product.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.analytics.product.dto.query.ProductPerformanceProductQueryResult;

public record ProductPerformanceItemResponse(
	int rankNo,
	Long productId,
	String productName,
	String brandName,
	Long categoryId,
	String categoryName,
	Long viewCount,
	Long orderCount,
	Long orderQuantity,
	Long salesAmount,
	BigDecimal viewToPurchaseRate,
	BigDecimal salesShareRate
) {

	public static ProductPerformanceItemResponse of(
		int rankNo,
		ProductPerformanceProductQueryResult result,
		BigDecimal viewToPurchaseRate,
		BigDecimal salesShareRate
	) {
		return new ProductPerformanceItemResponse(
			rankNo,
			result.productId(),
			result.productName(),
			result.brandName(),
			result.categoryId(),
			result.categoryName(),
			result.viewCount(),
			result.orderCount(),
			result.orderQuantity(),
			result.salesAmount(),
			viewToPurchaseRate,
			salesShareRate
		);
	}
}