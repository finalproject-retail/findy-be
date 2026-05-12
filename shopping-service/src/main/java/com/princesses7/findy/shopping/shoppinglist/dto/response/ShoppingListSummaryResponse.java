package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

public record ShoppingListSummaryResponse(
	Long shoppingListId,
	Long cartId,
	int totalItemCount,
	long scannedItemCount
) {

	public static ShoppingListSummaryResponse from(ShoppingList shoppingList) {
		return new ShoppingListSummaryResponse(
			shoppingList.getShoppingListId(),
			shoppingList.getCart().getCartId(),
			shoppingList.getTotalItemCount(),
			shoppingList.getScannedItemCount()
		);
	}
}