package com.princesses7.findy.shopping.product.dto.response;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.product.entity.Product;

public record ProductDetailResponse(
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
	com.princesses7.findy.shopping.product.entity.SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockUnit
) {

	public static ProductDetailResponse from(Product product, Inventory inventory) {
		return new ProductDetailResponse(
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
			product.getSaleStatus(),
			product.getGridId(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus().name(),
			inventory == null ? null : inventory.getUnit()
		);
	}
}
