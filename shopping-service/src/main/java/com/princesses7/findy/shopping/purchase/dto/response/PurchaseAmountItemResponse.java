package com.princesses7.findy.shopping.purchase.dto.response;

public record PurchaseAmountItemResponse(
	Long productId,
	int quantity,
	int productPrice,
	int totalAmount,
	int discountAmount,
	int finalAmount
) {
}