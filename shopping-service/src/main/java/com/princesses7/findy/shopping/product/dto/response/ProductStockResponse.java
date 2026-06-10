package com.princesses7.findy.shopping.product.dto.response;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;

public record ProductStockResponse(
	Long productId,
	String productName,
	Long storeId,
	Integer stockQuantity,
	String stockStatus,
	String stockUnit,
	String stockBadgeText
) {

	public static ProductStockResponse from(Product product, Inventory inventory) {
		return new ProductStockResponse(
			product.getProductId(),
			product.getProductName(),
			inventory.getStoreId(),
			inventory.getStockQuantity(),
			inventory.getStockStatus().name(),
			inventory.getUnit(),
			createStockBadgeText(inventory)
		);
	}

	private static String createStockBadgeText(Inventory inventory) {
		if (inventory.getStockStatus() == StockStatus.OUT_OF_STOCK) {
			return "품절";
		}

		if (inventory.getStockStatus() == StockStatus.LOW_STOCK) {
			return "품절임박";
		}

		return "남은 재고 " + inventory.getStockQuantity() + inventory.getUnit();
	}
}