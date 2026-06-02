package com.princesses7.findy.user.recentview.dto.response;

import java.math.BigDecimal;

public record ProductSummaryResponse(
	Long productId,
	String brandName,
	String productName,
	String barcode,
	String imageUrl,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	String saleStatus,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {
}