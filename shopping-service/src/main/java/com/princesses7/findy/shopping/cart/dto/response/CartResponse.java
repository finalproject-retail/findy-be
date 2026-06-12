package com.princesses7.findy.shopping.cart.dto.response;

import java.util.List;
import java.util.Map;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;

public record CartResponse(
	Long cartId,
	Long userId,
	List<CartItemResponse> items,
	int totalItemCount,
	int checkedItemCount,
	int totalQuantity,
	int totalAmount,
	int checkedTotalAmount
) {

	public static CartResponse from(
		Cart cart,
		Map<Long, ProductSummaryResponse> productMap
	) {
		List<CartItemResponse> items = cart.getCartItems()
			.stream()
			.filter(cartItem -> productMap.containsKey(cartItem.getProductId()))
			.map(cartItem -> CartItemResponse.from(
				cartItem,
				productMap.get(cartItem.getProductId())
			))
			.toList();

		int checkedItemCount = (int)items
			.stream()
			.filter(CartItemResponse::checked)
			.count();

		int totalQuantity = items
			.stream()
			.mapToInt(CartItemResponse::quantity)
			.sum();

		int totalAmount = items.stream()
			.mapToInt(CartItemResponse::itemTotalAmount)
			.sum();

		int checkedTotalAmount = items.stream()
			.filter(CartItemResponse::checked)
			.mapToInt(CartItemResponse::itemTotalAmount)
			.sum();

		return new CartResponse(
			cart.getCartId(),
			cart.getUserId(),
			items,
			items.size(),
			checkedItemCount,
			totalQuantity,
			totalAmount,
			checkedTotalAmount
		);
	}

	public static CartResponse empty(Long userId) {
		return new CartResponse(
			null,
			userId,
			List.of(),
			0,
			0,
			0,
			0,
			0
		);
	}
}
