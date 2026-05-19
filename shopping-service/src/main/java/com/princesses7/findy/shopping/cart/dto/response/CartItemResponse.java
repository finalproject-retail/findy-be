package com.princesses7.findy.shopping.cart.dto.response;

import com.princesses7.findy.shopping.cart.entity.CartItem;

public record CartItemResponse(
	Long cartItemId,
	Long productId,
	int quantity,
	boolean checked
) {

	public static CartItemResponse from(CartItem cartItem) {
		return new CartItemResponse(
			cartItem.getCartItemId(),
			cartItem.getProductId(),
			cartItem.getQuantity(),
			cartItem.isChecked()
		);
	}
}