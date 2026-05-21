package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record ProductSummaryResponse(
	Long productId,
	String brandName,
	String productName,
	String imageUrl,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	SaleStatus saleStatus,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {

	public static ProductSummaryResponse from(Product product, Inventory inventory) {
		return new ProductSummaryResponse(
			product.getProductId(),
			product.getBrandName(),
			product.getProductName(),
			product.getImageUrl(),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			product.getSaleStatus(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus().name(),
			createStockBadgeText(inventory)
		);
	}

	public int calculateAmount(int quantity) {
		if (salePrice == null) {
			return 0;
		}

		return salePrice * quantity;
	}

	private static String createStockBadgeText(Inventory inventory) {
		if (inventory == null) {
			return "재고 확인 불가";
		}

		if (inventory.getStockStatus() == StockStatus.OUT_OF_STOCK) {
			return "품절";
		}

		if (inventory.getStockStatus() == StockStatus.LOW_STOCK) {
			return "품절임박";
		}

		return "남은 재고 " + inventory.getStockQuantity() + "개";
	}
}