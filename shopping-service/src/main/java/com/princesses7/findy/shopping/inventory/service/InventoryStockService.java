package com.princesses7.findy.shopping.inventory.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.exception.InventoryException;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryStockService {

	private final InventoryRepository inventoryRepository;

	/**
	 * 기존 주문 완료 시점 재고 차감 메서드.
	 * 쇼핑리스트 생성 시점에 재고를 예약 차감하는 구조로 바꾸면
	 * OrderService에서는 이 메서드를 더 이상 호출하면 안 됨.
	 */
	public void decreaseStocks(Long storeId, List<OrderItem> orderItems) {
		Map<Long, Integer> quantityByProductId = orderItems.stream()
			.collect(Collectors.groupingBy(
				OrderItem::getProductId,
				Collectors.summingInt(OrderItem::getQuantity)
			));

		decreaseStocksByProductId(storeId, quantityByProductId);
	}

	public void decreaseStock(Long storeId, Long productId, int quantity) {
		decreaseStocksByProductId(storeId, Map.of(productId, quantity));
	}

	public void increaseStock(Long storeId, Long productId, int quantity) {
		increaseStocksByProductId(storeId, Map.of(productId, quantity));
	}

	public void decreaseStocksByCartItems(Long storeId, Collection<CartItem> cartItems) {
		Map<Long, Integer> quantityByProductId = cartItems.stream()
			.collect(Collectors.groupingBy(
				CartItem::getProductId,
				Collectors.summingInt(CartItem::getQuantity)
			));

		decreaseStocksByProductId(storeId, quantityByProductId);
	}

	public void increaseStocks(Long storeId, Collection<ShoppingListItem> shoppingListItems) {
		Map<Long, Integer> quantityByProductId = shoppingListItems.stream()
			.collect(Collectors.groupingBy(
				ShoppingListItem::getProductId,
				Collectors.summingInt(ShoppingListItem::getQuantity)
			));

		increaseStocksByProductId(storeId, quantityByProductId);
	}

	public void increaseStocksByShoppingListItems(
		Long storeId,
		Collection<ShoppingListItem> shoppingListItems
	) {
		increaseStocks(storeId, shoppingListItems);
	}

	public void increaseUnscannedStocksByShoppingListItems(
		Long storeId,
		Collection<ShoppingListItem> shoppingListItems
	) {
		Map<Long, Integer> quantityByProductId = shoppingListItems.stream()
			.collect(Collectors.groupingBy(
				ShoppingListItem::getProductId,
				Collectors.summingInt(item -> Math.max(
					item.getQuantity() - item.getScannedQuantity(),
					0
				))
			));

		quantityByProductId.entrySet()
			.removeIf(entry -> entry.getValue() < 1);

		increaseStocksByProductId(storeId, quantityByProductId);
	}

	private void decreaseStocksByProductId(
		Long storeId,
		Map<Long, Integer> quantityByProductId
	) {
		if (quantityByProductId.isEmpty()) {
			return;
		}

		Map<Long, Inventory> inventoryByProductId = getInventoryByProductId(
			storeId,
			quantityByProductId.keySet()
		);

		quantityByProductId.forEach((productId, quantity) -> {
			Inventory inventory = inventoryByProductId.get(productId);

			if (inventory == null) {
				throw new InventoryException(INVENTORY_NOT_FOUND);
			}

			inventory.decreaseStock(quantity);
		});
	}

	private void increaseStocksByProductId(
		Long storeId,
		Map<Long, Integer> quantityByProductId
	) {
		if (quantityByProductId.isEmpty()) {
			return;
		}

		Map<Long, Inventory> inventoryByProductId = getInventoryByProductId(
			storeId,
			quantityByProductId.keySet()
		);

		quantityByProductId.forEach((productId, quantity) -> {
			Inventory inventory = inventoryByProductId.get(productId);

			if (inventory == null) {
				throw new InventoryException(INVENTORY_NOT_FOUND);
			}

			inventory.increaseStock(quantity);
		});
	}

	private Map<Long, Inventory> getInventoryByProductId(
		Long storeId,
		Collection<Long> productIds
	) {
		return inventoryRepository.findAllByProductProductIdInAndStoreId(productIds, storeId)
			.stream()
			.collect(Collectors.toMap(
				Inventory::getProductId,
				Function.identity()
			));
	}
}