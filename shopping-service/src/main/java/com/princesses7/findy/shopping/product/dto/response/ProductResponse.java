package com.princesses7.findy.shopping.product.dto.response;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record ProductResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String barcode,
	Integer originalPrice,
	String description,
	String imageUrl,
	String salesUnit,
	String volume,
	String allergyInfo,
	String badgeText,
	SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {

	public static ProductResponse from(Product product, Inventory inventory) {
		return new ProductResponse(
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			product.getOriginalPrice(),
			product.getDescription(),
			product.getImageUrl(),
			product.getSalesUnit(),
			product.getVolume(),
			product.getAllergyInfo(),
			product.getBadgeText(),
			product.getSaleStatus(),
			product.getGridId(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus().name(),
			createStockBadgeText(inventory)
		);
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