package com.princesses7.findy.recommendation.external.shopping.dto;

import java.time.LocalDateTime;

public record FrequentPurchaseProductResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String imageUrl,
	Integer originalPrice,
	String saleStatus,
	Long gridId,
	Long purchaseCount,
	Long purchaseQuantity,
	LocalDateTime lastPurchasedAt
) {
}
