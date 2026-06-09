package com.princesses7.findy.shopping.purchase.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.inventory.entity.Inventory;
import com.princesses7.findy.shopping.inventory.repository.InventoryRepository;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseTargetItemResponse;
import com.princesses7.findy.shopping.purchase.dto.response.PurchaseTargetResponse;
import com.princesses7.findy.shopping.purchase.exception.PurchaseException;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.repository.ShoppingListRepository;
import com.princesses7.findy.shopping.store.StoreIdSupport;

@Service
@Transactional(readOnly = true)
public class PurchaseTargetService {

	private final ShoppingListRepository shoppingListRepository;
	private final InventoryRepository inventoryRepository;

	public PurchaseTargetService(
		ShoppingListRepository shoppingListRepository,
		InventoryRepository inventoryRepository
	) {
		this.shoppingListRepository = shoppingListRepository;
		this.inventoryRepository = inventoryRepository;
	}

	public PurchaseTargetResponse getPurchaseTargets(Long userId, long storeId) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
			.orElseThrow(() -> new PurchaseException(SHOPPING_LIST_NOT_FOUND));

		List<PurchaseTargetItemResponse> items = new ArrayList<>();
		for (ShoppingListItem item : shoppingList.getShoppingListItems()) {
			if (hasScannedQuantity(item)) {
				items.add(toPurchaseTargetItem(item, resolvedStoreId));
			}
		}

		if (items.isEmpty()) {
			throw new PurchaseException(NO_SCANNED_ITEM);
		}

		int totalPurchaseQuantity = items.stream()
			.mapToInt(PurchaseTargetItemResponse::purchaseQuantity)
			.sum();

		return new PurchaseTargetResponse(
			shoppingList.getShoppingListId(),
			shoppingList.getUserId(),
			totalPurchaseQuantity,
			items
		);
	}

	private boolean hasScannedQuantity(ShoppingListItem item) {
		return item.getScannedQuantity() > 0;
	}

	private PurchaseTargetItemResponse toPurchaseTargetItem(ShoppingListItem item, long storeId) {
		Inventory inventory = getInventory(item.getProductId(), storeId);
		Integer stockQuantity = inventory.getStockQuantity();

		return new PurchaseTargetItemResponse(
			item.getShoppingListItemId(),
			item.getProductId(),
			item.getQuantity(),
			item.getScannedQuantity(),
			stockQuantity == null ? 0 : stockQuantity
		);
	}

	private Inventory getInventory(Long productId, long storeId) {
		return inventoryRepository.findByProductProductIdAndStoreId(productId, storeId)
			.orElseThrow(() -> new PurchaseException(PRODUCT_STOCK_NOT_FOUND));
	}
}
