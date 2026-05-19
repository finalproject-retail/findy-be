package com.princesses7.findy.shopping.product.dto.command;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.product.entity.SaleStatus;

public record ProductImportCommand(
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
	SaleStatus saleStatus
) {
}