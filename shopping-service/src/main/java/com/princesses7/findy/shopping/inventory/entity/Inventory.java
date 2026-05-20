package com.princesses7.findy.shopping.inventory.entity;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.product.entity.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "inventories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseTimeEntity {

	private static final int LOW_STOCK_THRESHOLD = 5;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "inventory_id")
	private Long inventoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(name = "store_id", nullable = false)
	private Long storeId;

	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	@Column(name = "unit", nullable = false, length = 30)
	private String unit;

	@Enumerated(EnumType.STRING)
	@Column(name = "stock_status", nullable = false, length = 30)
	private StockStatus stockStatus;

	public static Inventory createDefault(Product product, Long storeId, Integer stockQuantity) {
		Inventory inventory = new Inventory();
		inventory.product = product;
		inventory.storeId = storeId;
		inventory.stockQuantity = stockQuantity;
		inventory.unit = "개";
		inventory.stockStatus = resolveStockStatus(stockQuantity);
		return inventory;
	}

	private static StockStatus resolveStockStatus(Integer stockQuantity) {
		if (stockQuantity == null || stockQuantity <= 0) {
			return StockStatus.OUT_OF_STOCK;
		}

		if (stockQuantity <= LOW_STOCK_THRESHOLD) {
			return StockStatus.LOW_STOCK;
		}

		return StockStatus.IN_STOCK;
	}
}