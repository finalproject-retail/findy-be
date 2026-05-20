package com.princesses7.findy.shopping.purchase.dto.response;

public record PurchaseTargetItemResponse(
	Long shoppingListItemId,
	Long productId,
	int listQuantity,
	int purchaseQuantity,
	int stockQuantity
) {
}