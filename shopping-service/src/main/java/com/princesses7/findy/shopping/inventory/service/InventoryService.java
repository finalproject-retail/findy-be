package com.princesses7.findy.shopping.inventory.service;

import java.util.concurrent.ThreadLocalRandom;

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
	private static final int MIN_DEFAULT_STOCK_QUANTITY = 2;
	private static final int MAX_DEFAULT_STOCK_QUANTITY = 30;

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
			createRandomDefaultStockQuantity()
		);

		inventoryRepository.save(inventory);
	}

	private int createRandomDefaultStockQuantity() {
		return ThreadLocalRandom.current()
			.nextInt(MIN_DEFAULT_STOCK_QUANTITY, MAX_DEFAULT_STOCK_QUANTITY + 1);
	}
}