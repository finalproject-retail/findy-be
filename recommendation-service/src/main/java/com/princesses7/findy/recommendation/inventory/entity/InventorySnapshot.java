package com.princesses7.findy.recommendation.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "inventories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventorySnapshot {

	private static final int LOW_STOCK_THRESHOLD = 5;

	private static final String IN_STOCK = "IN_STOCK";
	private static final String NORMAL = "NORMAL";
	private static final String LOW_STOCK = "LOW_STOCK";
	private static final String OUT_OF_STOCK = "OUT_OF_STOCK";
	private static final String SOLD_OUT = "SOLD_OUT";

	@Id
	@Column(name = "inventory_id")
	private Long inventoryId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "store_id", nullable = false)
	private Long storeId;

	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@Column(name = "unit", nullable = false)
	private String unit;

	@Column(name = "stock_status", nullable = false)
	private String stockStatus;

	public boolean hasAvailableStock() {
		return stockQuantity != null
			&& stockQuantity > 0
			&& !isOutOfStock();
	}

	public boolean needsSubstituteRecommendation() {
		return isOutOfStock()
			|| isLowStock()
			|| isLowStockByQuantity();
	}

	public boolean isOutOfStock() {
		return OUT_OF_STOCK.equals(stockStatus)
			|| SOLD_OUT.equals(stockStatus)
			|| stockQuantity == null
			|| stockQuantity <= 0;
	}

	public boolean isLowStock() {
		return LOW_STOCK.equals(stockStatus);
	}

	public boolean isNormalStock() {
		return IN_STOCK.equals(stockStatus)
			|| NORMAL.equals(stockStatus);
	}

	private boolean isLowStockByQuantity() {
		return stockQuantity != null
			&& stockQuantity > 0
			&& stockQuantity <= LOW_STOCK_THRESHOLD;
	}
}