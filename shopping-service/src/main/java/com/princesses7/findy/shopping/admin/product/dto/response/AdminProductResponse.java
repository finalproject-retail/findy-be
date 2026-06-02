package com.princesses7.findy.shopping.admin.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record AdminProductResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String barcode,
	String externalSource,
	String externalProductId,
	Integer originalPrice,
	String imageUrl,
	SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockUnit,
	BigDecimal categoryConfidence,
	String categoryClassifiedBy,
	Boolean categoryReviewRequired,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static AdminProductResponse from(Product product, Inventory inventory) {
		return new AdminProductResponse(
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			product.getExternalSource(),
			product.getExternalProductId(),
			product.getOriginalPrice(),
			product.getImageUrl(),
			product.getSaleStatus(),
			product.getGridId(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus().name(),
			inventory == null ? null : inventory.getUnit(),
			product.getCategoryConfidence(),
			product.getCategoryClassifiedBy(),
			product.getCategoryReviewRequired(),
			product.getCreatedAt(),
			product.getUpdatedAt()
		);
	}
}