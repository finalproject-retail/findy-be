package com.princesses7.findy.shopping.order.dto.response;

import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;

public record OrderItemResponse(
	Long orderItemId,
	Long productId,
	String productName,
	String imageUrl,
	ProductSummaryResponse product,
	int quantity,
	int productPrice,
	int itemDiscountAmount,
	int discountAmount,
	int itemFinalAmount,
	int finalAmount
) {

	public static OrderItemResponse from(
		OrderItem orderItem,
		ProductSummaryResponse product
	) {
		return new OrderItemResponse(
			orderItem.getOrderItemId(),
			orderItem.getProductId(),
			resolveProductName(orderItem, product),
			resolveImageUrl(product),
			product,
			orderItem.getQuantity(),
			orderItem.getProductPrice(),
			orderItem.getDiscountAmount(),
			orderItem.getDiscountAmount(),
			orderItem.getFinalAmount(),
			orderItem.getFinalAmount()
		);
	}

	private static String resolveProductName(
		OrderItem orderItem,
		ProductSummaryResponse product
	) {
		if (product != null && product.productName() != null && !product.productName().isBlank()) {
			return product.productName();
		}

		return "상품 #" + orderItem.getProductId();
	}

	private static String resolveImageUrl(ProductSummaryResponse product) {
		if (product == null) {
			return null;
		}

		return product.imageUrl();
	}
}