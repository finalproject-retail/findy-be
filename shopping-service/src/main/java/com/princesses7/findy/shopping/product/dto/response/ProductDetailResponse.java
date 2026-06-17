package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;

public record ProductDetailResponse(
	Long productId,
	Long categoryId,
	String brandName,
	String productName,
	String barcode,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	Long promotionId,
	String promotionName,
	String description,
	String imageUrl,
	String salesUnit,
	String volume,
	String allergyInfo,
	SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockUnit
) {

	public static ProductDetailResponse from(Product product, Inventory inventory) {
		return from(product, inventory, null);
	}

	public static ProductDetailResponse from(
		Product product,
		Inventory inventory,
		PromotionProduct promotionProduct
	) {
		Integer originalPrice = product.getOriginalPrice();
		Integer salePrice = resolveSalePrice(originalPrice, promotionProduct);
		BigDecimal discountRate = calculateDiscountRate(originalPrice, salePrice);

		Promotion promotion = promotionProduct == null ? null : promotionProduct.getPromotion();

		return new ProductDetailResponse(
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			originalPrice,
			salePrice,
			discountRate,
			promotion == null ? null : promotion.getPromotionId(),
			promotion == null ? null : promotion.getPromotionName(),
			product.getDescription(),
			product.getImageUrl(),
			product.getSalesUnit(),
			product.getVolume(),
			product.getAllergyInfo(),
			product.getSaleStatus(),
			product.getGridId(),
			inventory == null ? null : inventory.getStockQuantity(),
			inventory == null || inventory.getStockStatus() == null
				? null
				: inventory.getStockStatus().name(),
			inventory == null ? null : inventory.getUnit()
		);
	}

	private static Integer resolveSalePrice(
		Integer originalPrice,
		PromotionProduct promotionProduct
	) {
		if (originalPrice == null) {
			return null;
		}

		if (promotionProduct == null || promotionProduct.getPromotionPrice() == null) {
			return originalPrice;
		}

		Integer promotionPrice = promotionProduct.getPromotionPrice();

		if (promotionPrice <= 0 || promotionPrice >= originalPrice) {
			return originalPrice;
		}

		return promotionPrice;
	}

	private static BigDecimal calculateDiscountRate(
		Integer originalPrice,
		Integer salePrice
	) {
		if (
			originalPrice == null ||
				originalPrice <= 0 ||
				salePrice == null ||
				salePrice >= originalPrice
		) {
			return BigDecimal.ZERO;
		}

		return BigDecimal.valueOf(originalPrice - salePrice)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(originalPrice), 2, RoundingMode.HALF_UP);
	}
}