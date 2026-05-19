package com.princesses7.findy.shopping.cart.dto.response;

import java.util.List;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;

public record CartResponse(
	Long cartId,
	Long userId,
	List<CartItemResponse> items,
	int totalItemCount,
	int checkedItemCount,
	int totalQuantity
) {

	public static CartResponse from(Cart cart) {
		List<CartItemResponse> items = cart.getCartItems()
			.stream()
			.map(CartItemResponse::from)
			.toList();

		int checkedItemCount = (int)cart.getCartItems()
			.stream()
			.filter(CartItem::isChecked)
			.count();

		int totalQuantity = cart.getCartItems()
			.stream()
			.mapToInt(CartItem::getQuantity)
			.sum();

		return new CartResponse(
			cart.getCartId(),
			cart.getUserId(),
			items,
			items.size(),
			checkedItemCount,
			totalQuantity
		);
	}

	public static CartResponse empty(Long userId) {
		return new CartResponse(
			null,
			userId,
			List.of(),
			0,
			0,
			0
		);
	}
}