package com.princesses7.findy.shopping.cart.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.cart.repository.CartItemRepository;
import com.princesses7.findy.shopping.order.entity.OrderItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartCleanupService {

	private final CartItemRepository cartItemRepository;

	public void cleanupPurchasedCartItems(Long userId, List<OrderItem> orderItems) {
		Map<Long, Integer> quantityByProductId = orderItems.stream()
			.collect(Collectors.groupingBy(
				OrderItem::getProductId,
				Collectors.summingInt(OrderItem::getQuantity)
			));

		if (quantityByProductId.isEmpty()) {
			return;
		}

		Map<Long, CartItem> cartItemByProductId = cartItemRepository
			.findAllByCartUserIdAndProductIdIn(userId, quantityByProductId.keySet())
			.stream()
			.collect(Collectors.toMap(
				CartItem::getProductId,
				Function.identity()
			));

		quantityByProductId.forEach((productId, purchasedQuantity) -> {
			CartItem cartItem = cartItemByProductId.get(productId);

			if (cartItem == null) {
				return;
			}

			if (cartItem.isFullyPurchased(purchasedQuantity)) {
				cartItemRepository.delete(cartItem);
				return;
			}

			cartItem.decreaseQuantityAfterPurchase(purchasedQuantity);
		});
	}
}