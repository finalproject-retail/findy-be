package com.princesses7.findy.shopping.order.dto.response;

public record OrderItemResponse(
	Long orderItemId,
	Long productId,
	int quantity,
	int productPrice,
	int discountAmount,
	int finalAmount
) {
}