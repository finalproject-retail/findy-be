package com.princesses7.findy.shopping.recentview.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.recentview.entity.RecentViewProduct;

public record RecentViewProductResponse(
	Long recentViewId,
	LocalDateTime viewedAt,
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String barcode,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	String imageUrl,
	String badgeText,
	SaleStatus saleStatus,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {

	public static RecentViewProductResponse from(
		RecentViewProduct recentViewProduct,
		Inventory inventory
	) {
		Product product = recentViewProduct.getProduct();

		return new RecentViewProductResponse(
			recentViewProduct.getRecentViewId(),
			recentViewProduct.getViewedAt(),
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			product.getImageUrl(),
			product.getBadgeText(),
			product.getSaleStatus(),
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