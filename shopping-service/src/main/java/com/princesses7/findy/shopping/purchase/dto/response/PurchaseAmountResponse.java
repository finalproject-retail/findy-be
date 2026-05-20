package com.princesses7.findy.shopping.purchase.dto.response;

import java.util.List;

public record PurchaseAmountResponse(
	Long userId,
	Long shoppingListId,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	List<PurchaseAmountItemResponse> items
) {
}