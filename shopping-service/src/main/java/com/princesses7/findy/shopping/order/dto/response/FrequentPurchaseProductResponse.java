package com.princesses7.findy.shopping.order.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.order.repository.projection.FrequentPurchaseProductRow;

public record FrequentPurchaseProductResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String imageUrl,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	Long purchaseCount,
	Long purchaseQuantity,
	LocalDateTime lastPurchasedAt
) {

	public static FrequentPurchaseProductResponse from(FrequentPurchaseProductRow row) {
		return new FrequentPurchaseProductResponse(
			row.productId(),
			row.categoryId(),
			row.brandName(),
			row.productName(),
			row.imageUrl(),
			row.originalPrice(),
			row.salePrice(),
			row.discountRate(),
			row.purchaseCount(),
			row.purchaseQuantity(),
			row.lastPurchasedAt()
		);
	}
}