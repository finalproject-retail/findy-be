package com.princesses7.findy.shopping.order.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.order.repository.projection.FrequentPurchaseProductRow;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record FrequentPurchaseProductResponse(
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

	public static FrequentPurchaseProductResponse from(FrequentPurchaseProductRow row) {
		return new FrequentPurchaseProductResponse(
			row.productId(),
			row.categoryId(),
			row.brandName(),
			row.productName(),
			row.imageUrl(),
			row.originalPrice(),
			row.saleStatus(),
			row.gridId(),
			row.purchaseCount(),
			row.purchaseQuantity(),
			row.lastPurchasedAt()
		);
	}
}
