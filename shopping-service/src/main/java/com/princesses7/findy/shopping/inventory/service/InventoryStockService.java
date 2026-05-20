package com.princesses7.findy.shopping.inventory.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.exception.InventoryException;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.order.entity.OrderItem;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryStockService {

	private final InventoryRepository inventoryRepository;

	public void decreaseStocks(Long storeId, List<OrderItem> orderItems) {
		Map<Long, Integer> quantityByProductId = orderItems.stream()
			.collect(Collectors.groupingBy(
				OrderItem::getProductId,
				Collectors.summingInt(OrderItem::getQuantity)
			));

		if (quantityByProductId.isEmpty()) {
			return;
		}

		Map<Long, Inventory> inventoryByProductId = inventoryRepository
			.findAllByProductProductIdInAndStoreId(quantityByProductId.keySet(), storeId)
			.stream()
			.collect(Collectors.toMap(
				Inventory::getProductId,
				Function.identity()
			));

		quantityByProductId.forEach((productId, quantity) -> {
			Inventory inventory = inventoryByProductId.get(productId);

			if (inventory == null) {
				throw new InventoryException(INVENTORY_NOT_FOUND);
			}

			inventory.decreaseStock(quantity);
		});
	}
}