package com.princesses7.findy.shopping.cart.dto.response;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;

public record CartItemResponse(
	Long cartItemId,
	Long productId,
	ProductSummaryResponse product,
	int quantity,
	boolean checked,
	int itemTotalAmount
) {

	public static CartItemResponse from(
		CartItem cartItem,
		ProductSummaryResponse product
	) {
		return new CartItemResponse(
			cartItem.getCartItemId(),
			cartItem.getProductId(),
			product,
			cartItem.getQuantity(),
			cartItem.isChecked(),
			product.calculateAmount(cartItem.getQuantity())
		);
	}
}