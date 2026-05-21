package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;

public record ShoppingListItemResponse(
	Long shoppingListItemId,
	Long productId,
	ProductSummaryResponse product,
	int quantity,
	int scannedQuantity,
	ScanStatus scanStatus,
	int itemTotalAmount,
	int scannedAmount
) {

	public static ShoppingListItemResponse from(
		ShoppingListItem item,
		ProductSummaryResponse product
	) {
		return new ShoppingListItemResponse(
			item.getShoppingListItemId(),
			item.getProductId(),
			product,
			item.getQuantity(),
			item.getScannedQuantity(),
			item.getScanStatus(),
			product.calculateAmount(item.getQuantity()),
			product.calculateAmount(item.getScannedQuantity())
		);
	}
}