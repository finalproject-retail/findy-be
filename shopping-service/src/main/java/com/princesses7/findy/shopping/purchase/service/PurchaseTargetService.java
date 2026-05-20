package com.princesses7.findy.shopping.purchase.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseTargetService {

	private static final Long DEFAULT_STORE_ID = 1L;

	private final ShoppingListRepository shoppingListRepository;
	private final InventoryRepository inventoryRepository;

	public PurchaseTargetResponse getPurchaseTargets(Long userId) {
		ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
			.orElseThrow(() -> new PurchaseException(SHOPPING_LIST_NOT_FOUND));

		List<PurchaseTargetItemResponse> items = shoppingList.getShoppingListItems().stream()
			.filter(this::hasScannedQuantity)
			.map(this::toPurchaseTargetItem)
			.toList();

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

	private PurchaseTargetItemResponse toPurchaseTargetItem(ShoppingListItem item) {
		Inventory inventory = getInventory(item.getProductId());

		validateStock(item, inventory);

		return new PurchaseTargetItemResponse(
			item.getShoppingListItemId(),
			item.getProductId(),
			item.getQuantity(),
			item.getScannedQuantity(),
			inventory.getStockQuantity()
		);
	}

	private Inventory getInventory(Long productId) {
		return inventoryRepository.findByProductProductIdAndStoreId(productId, DEFAULT_STORE_ID)
			.orElseThrow(() -> new PurchaseException(PRODUCT_STOCK_NOT_FOUND));
	}

	private void validateStock(ShoppingListItem item, Inventory inventory) {
		if (inventory.getStockQuantity() < item.getScannedQuantity()) {
			throw new PurchaseException(PURCHASE_INSUFFICIENT_STOCK);
		}
	}
}