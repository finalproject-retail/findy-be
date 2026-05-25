package com.princesses7.findy.recommendation.recommendation.dto.response;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;

public record SourceInventoryResponse(
	Long inventoryId,
	Long productId,
	Long storeId,
	Integer stockQuantity,
	String unit,
	String stockStatus,
	boolean substituteRequired
) {

	public static SourceInventoryResponse from(InventorySnapshot inventory) {
		return new SourceInventoryResponse(
			inventory.getInventoryId(),
			inventory.getProductId(),
			inventory.getStoreId(),
			inventory.getStockQuantity(),
			inventory.getUnit(),
			inventory.getStockStatus(),
			inventory.needsSubstituteRecommendation()
		);
	}
}