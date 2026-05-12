package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;

public record ShoppingListItemResponse(
	Long shoppingListItemId,
	Long productId,
	Long cartItemId,
	int quantity,
	int scannedQuantity,
	ScanStatus scanStatus
) {

	public static ShoppingListItemResponse from(ShoppingListItem item) {
		return new ShoppingListItemResponse(
			item.getShoppingListItemId(),
			item.getProductId(),
			item.getCartItem() == null ? null : item.getCartItem().getCartItemId(),
			item.getQuantity(),
			item.getScannedQuantity(),
			item.getScanStatus()
		);
	}
}