package com.princesses7.findy.shopping.order.dto.response;

import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;

public record OrderItemResponse(
	Long orderItemId,
	Long productId,
	ProductSummaryResponse product,
	int quantity,
	int productPrice,
	int discountAmount,
	int finalAmount
) {

	public static OrderItemResponse from(
		OrderItem orderItem,
		ProductSummaryResponse product
	) {
		return new OrderItemResponse(
			orderItem.getOrderItemId(),
			orderItem.getProductId(),
			product,
			orderItem.getQuantity(),
			orderItem.getProductPrice(),
			orderItem.getDiscountAmount(),
			orderItem.getFinalAmount()
		);
	}
}