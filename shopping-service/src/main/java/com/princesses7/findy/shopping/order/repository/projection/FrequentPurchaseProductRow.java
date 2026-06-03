package com.princesses7.findy.shopping.order.repository.projection;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record FrequentPurchaseProductRow(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String imageUrl,
	Integer originalPrice,
	SaleStatus saleStatus,
	Long gridId,
	Long purchaseCount,
	Long purchaseQuantity,
	LocalDateTime lastPurchasedAt
) {
}
