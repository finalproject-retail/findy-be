package com.princesses7.findy.shopping.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.product.entity.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {

	private static final Long DEFAULT_STORE_ID = 1L;
	private static final int DEFAULT_STOCK_QUANTITY = 30;

	private final InventoryRepository inventoryRepository;

	@Transactional
	public void createDefaultInventory(Product product) {
		if (inventoryRepository.existsByProductProductIdAndStoreId(
			product.getProductId(),
			DEFAULT_STORE_ID
		)) {
			return;
		}

		Inventory inventory = Inventory.createDefault(
			product,
			DEFAULT_STORE_ID,
			DEFAULT_STOCK_QUANTITY
		);

		inventoryRepository.save(inventory);
	}
}