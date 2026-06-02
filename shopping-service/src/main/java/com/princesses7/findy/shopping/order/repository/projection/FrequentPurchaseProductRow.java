package com.princesses7.findy.shopping.order.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FrequentPurchaseProductRow(
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
}