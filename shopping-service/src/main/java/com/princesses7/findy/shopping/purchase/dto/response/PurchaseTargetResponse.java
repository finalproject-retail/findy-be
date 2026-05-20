package com.princesses7.findy.shopping.purchase.dto.response;

import java.util.List;

public record PurchaseTargetResponse(
	Long shoppingListId,
	Long userId,
	int totalPurchaseQuantity,
	List<PurchaseTargetItemResponse> items
) {
}