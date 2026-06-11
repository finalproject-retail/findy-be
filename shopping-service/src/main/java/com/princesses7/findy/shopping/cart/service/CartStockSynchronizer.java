package com.princesses7.findy.shopping.cart.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;

@Component
public class CartStockSynchronizer {

	public void synchronize(Cart cart, Map<Long, ProductSummaryResponse> productMap) {
		cart.getCartItems()
			.forEach(cartItem -> synchronizeItem(
				cartItem,
				productMap.get(cartItem.getProductId())
			));
	}

	private void synchronizeItem(CartItem cartItem, ProductSummaryResponse product) {
		if (product == null || !product.isPurchasable()) {
			cartItem.uncheck();
			return;
		}

		int purchasableQuantity = product.purchasableQuantity();

		if (cartItem.hasQuantityGreaterThan(purchasableQuantity)) {
			cartItem.clampQuantityTo(purchasableQuantity);
		}
	}
}