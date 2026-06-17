package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.entity.StockStatus;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;

public record ProductResponse(
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
	String badgeText,
	SaleStatus saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus,
	String stockBadgeText
) {

	public static ProductResponse from(Product product, Inventory inventory) {
		return from(product, inventory, null);
	}

	public static ProductResponse from(
		Product product,
		Inventory inventory,
		PromotionProduct promotionProduct
	) {
		Integer originalPrice = product.getOriginalPrice();
		Integer salePrice = resolveSalePrice(product, promotionProduct);

		return new ProductResponse(
			product.getProductId(),
			product.getCategoryId(),
			product.getBrandName(),
			product.getProductName(),
			product.getBarcode(),
			originalPrice,
			salePrice,
			promotionProduct == null ? null : promotionProduct.getPromotion().getDiscountRate(),
			promotionProduct == null ? null : promotionProduct.getPromotion().getPromotionId(),
			promotionProduct == null ? null : promotionProduct.getPromotion().getPromotionName(),
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

	private static Integer resolveSalePrice(
		Product product,
		PromotionProduct promotionProduct
	) {
		if (promotionProduct != null && promotionProduct.getPromotionPrice() != null) {
			return promotionProduct.getPromotionPrice();
		}

		return product.getOriginalPrice();
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