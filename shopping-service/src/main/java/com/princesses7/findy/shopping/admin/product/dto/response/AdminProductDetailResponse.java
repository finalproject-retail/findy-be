package com.princesses7.findy.shopping.admin.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record AdminProductDetailResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String barcode,
	String externalSource,
	String externalProductId,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	String description,
	String imageUrl,
	String packagingType,
	String salesUnit,
	String volume,
	String allergyInfo,
	String badgeText,
	SaleStatus saleStatus,
	Integer stockQuantity,
	String stockStatus,
	String stockUnit,
	BigDecimal categoryConfidence,
	String categoryClassifiedBy,
	Boolean categoryReviewRequired,
	Boolean isDeleted,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {

	public static AdminProductDetailResponse from(Product product, Inventory inventory) {
		return new AdminProductDetailResponse(
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			product.getExternalSource(),
			product.getExternalProductId(),
			product.getOriginalPrice(),
			product.getSalePrice(),
			product.getDiscountRate(),
			product.getDescription(),
			product.getImageUrl(),
			product.getPackagingType(),
			product.getSalesUnit(),
			product.getVolume(),
			product.getAllergyInfo(),
			product.getBadgeText(),
			product.getSaleStatus(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null ? null : inventory.getStockStatus().name(),
			inventory == null ? null : inventory.getUnit(),
			product.getCategoryConfidence(),
			product.getCategoryClassifiedBy(),
			product.getCategoryReviewRequired(),
			product.getIsDeleted(),
			product.getCreatedAt(),
			product.getUpdatedAt()
		);
	}
}