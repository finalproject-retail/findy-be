package com.princesses7.findy.shopping.shoppinglist.dto.response;

import java.util.List;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

public record ShoppingListResponse(
	Long shoppingListId,
	Long cartId,
	int totalItemCount,
	long scannedItemCount,
	List<ShoppingListItemResponse> items
) {

	public static ShoppingListResponse from(ShoppingList shoppingList) {
		return new ShoppingListResponse(
			shoppingList.getShoppingListId(),
			shoppingList.getCart().getCartId(),
			shoppingList.getTotalItemCount(),
			shoppingList.getScannedItemCount(),
			shoppingList.getShoppingListItems().stream()
				.map(ShoppingListItemResponse::from)
				.toList()
		);
	}
}