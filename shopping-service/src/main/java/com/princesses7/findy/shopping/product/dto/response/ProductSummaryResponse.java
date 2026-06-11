package com.princesses7.findy.shopping.product.dto.response;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record ProductSummaryResponse(
	Long productId,
	String brandName,
	String productName,
	String barcode,
	String imageUrl,
	Integer originalPrice,
	SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {

	public static ProductSummaryResponse from(Product product, Inventory inventory) {
		return new ProductSummaryResponse(
			product.getProductId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			product.getImageUrl(),
			product.getOriginalPrice(),
			product.getSaleStatus(),
			product.getGridId(),
			inventory == null ? null : inventory.getStockQuantity(),
			createStockStatus(product, inventory),
			createStockBadgeText(product, inventory)
		);
	}

	public int calculateAmount(int quantity) {
		if (originalPrice == null) {
			return 0;
		}

		return originalPrice * quantity;
	}

	public boolean isPurchasable() {
		return saleStatus == SaleStatus.ON_SALE
			&& stockQuantity != null
			&& stockQuantity > 0;
	}

	public int purchasableQuantity() {
		if (!isPurchasable()) {
			return 0;
		}

		return stockQuantity;
	}

	private static String createStockStatus(Product product, Inventory inventory) {
		if (product.getSaleStatus() != SaleStatus.ON_SALE) {
			return StockStatus.OUT_OF_STOCK.name();
		}

		if (inventory == null) {
			return null;
		}

		return inventory.getStockStatus().name();
	}

	private static String createStockBadgeText(Product product, Inventory inventory) {
		if (product.getSaleStatus() != SaleStatus.ON_SALE) {
			return "품절";
		}

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