package com.princesses7.findy.shopping.shoppinglist.dto.response;

import java.util.List;
import java.util.Map;

import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

public record ShoppingListResponse(
	Long shoppingListId,
	Long cartId,
	int totalItemCount,
	long scannedItemCount,
	int totalAmount,
	int scannedAmount,
	List<ShoppingListItemResponse> items
) {

	public static ShoppingListResponse from(
		ShoppingList shoppingList,
		Map<Long, ProductSummaryResponse> productMap
	) {
		List<ShoppingListItemResponse> items = shoppingList.getShoppingListItems()
			.stream()
			.map(item -> ShoppingListItemResponse.from(
				item,
				productMap.get(item.getProductId())
			))
			.toList();

		int totalAmount = items.stream()
			.mapToInt(ShoppingListItemResponse::itemTotalAmount)
			.sum();

		int scannedAmount = items.stream()
			.mapToInt(ShoppingListItemResponse::scannedAmount)
			.sum();

		return new ShoppingListResponse(
			shoppingList.getShoppingListId(),
			shoppingList.getCart().getCartId(),
			shoppingList.getTotalItemCount(),
			shoppingList.getScannedItemCount(),
			totalAmount,
			scannedAmount,
			items
		);
	}
}