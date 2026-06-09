package com.princesses7.findy.shopping.shoppinglist.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;
import java.util.Objects;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;
import com.princesses7.findy.shopping.shoppinglist.type.ShoppingListItemType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shopping_list_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingListItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopping_list_item_id")
	private Long shoppingListItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shopping_list_id", nullable = false)
	private ShoppingList shoppingList;

	@Enumerated(EnumType.STRING)
	@Column(name = "item_type", nullable = false, length = 30)
	private ShoppingListItemType itemType;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "category_name")
	private String categoryName;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "scanned_quantity", nullable = false)
	private int scannedQuantity;

	@Column(name = "is_checked", nullable = false)
	private boolean checked;

	@Column(name = "scanned_at")
	private LocalDateTime scannedAt;

	private ShoppingListItem(
		ShoppingList shoppingList,
		ShoppingListItemType itemType,
		Long productId,
		Long categoryId,
		String categoryName,
		int quantity,
		int scannedQuantity,
		boolean checked,
		LocalDateTime scannedAt
	) {
		validateQuantity(quantity);
		validateItemTarget(itemType, productId, categoryName);
		validateScannedQuantity(itemType, quantity, scannedQuantity);

		this.shoppingList = shoppingList;
		this.itemType = itemType;
		this.productId = productId;
		this.categoryId = categoryId;
		this.categoryName = normalizeCategoryName(categoryName);
		this.quantity = quantity;
		this.scannedQuantity = scannedQuantity;
		this.checked = resolveChecked(itemType, quantity, scannedQuantity, checked);
		this.scannedAt = scannedAt;
	}

	public static ShoppingListItem createFromCartItem(
		ShoppingList shoppingList,
		CartItem cartItem
	) {
		return createProductItem(
			shoppingList,
			cartItem.getProductId(),
			cartItem.getQuantity(),
			0,
			null
		);
	}

	public static ShoppingListItem createFromSearch(
		ShoppingList shoppingList,
		Long productId,
		int quantity
	) {
		return createProductItem(
			shoppingList,
			productId,
			quantity,
			0,
			null
		);
	}

	public static ShoppingListItem createFromScan(
		ShoppingList shoppingList,
		Long productId,
		int quantity
	) {
		return createProductItem(
			shoppingList,
			productId,
			quantity,
			quantity,
			LocalDateTime.now()
		);
	}

	public static ShoppingListItem createFromCategory(
		ShoppingList shoppingList,
		Long categoryId,
		String categoryName,
		int quantity
	) {
		return new ShoppingListItem(
			shoppingList,
			ShoppingListItemType.CATEGORY,
			null,
			categoryId,
			categoryName,
			quantity,
			0,
			false,
			null
		);
	}

	private static ShoppingListItem createProductItem(
		ShoppingList shoppingList,
		Long productId,
		int quantity,
		int scannedQuantity,
		LocalDateTime scannedAt
	) {
		return new ShoppingListItem(
			shoppingList,
			ShoppingListItemType.PRODUCT,
			productId,
			null,
			null,
			quantity,
			scannedQuantity,
			scannedQuantity == quantity,
			scannedAt
		);
	}

	public boolean hasSameId(Long shoppingListItemId) {
		return this.shoppingListItemId != null
			&& this.shoppingListItemId.equals(shoppingListItemId);
	}

	public boolean hasSameProduct(Long productId) {
		return isProductItem()
			&& this.productId != null
			&& this.productId.equals(productId);
	}

	public boolean hasSameCategory(Long categoryId, String categoryName) {
		if (!isCategoryItem()) {
			return false;
		}

		if (this.categoryId != null && categoryId != null) {
			return this.categoryId.equals(categoryId);
		}

		return Objects.equals(
			normalizeCategoryName(this.categoryName),
			normalizeCategoryName(categoryName)
		);
	}

	public boolean hasQuantity(int quantity) {
		return this.quantity == quantity;
	}

	public boolean isProductItem() {
		return itemType == ShoppingListItemType.PRODUCT && productId != null;
	}

	public boolean isCategoryItem() {
		return this.itemType == ShoppingListItemType.CATEGORY;
	}

	public boolean isScanned() {
		return getScanStatus() == ScanStatus.SCANNED;
	}

	public ScanStatus getScanStatus() {
		if (isCategoryItem()) {
			return checked ? ScanStatus.SCANNED : ScanStatus.NOT_SCANNED;
		}

		if (scannedQuantity == 0) {
			return ScanStatus.NOT_SCANNED;
		}

		if (scannedQuantity < quantity) {
			return ScanStatus.PARTIALLY_SCANNED;
		}

		return ScanStatus.SCANNED;
	}

	public int getCompletedQuantity() {
		if (isCategoryItem()) {
			return checked ? quantity : 0;
		}

		return scannedQuantity;
	}

	public void completeScan() {
		validateProductItem();

		if (scannedQuantity < quantity) {
			this.scannedQuantity++;
			updateScannedAt();
			return;
		}

		this.quantity++;
		this.scannedQuantity++;
		updateScannedAt();
	}

	public void scanOrIncreaseQuantity(int quantity) {
		validateProductItem();
		validateQuantity(quantity);

		for (int i = 0; i < quantity; i++) {
			completeScan();
		}
	}

	public void increaseQuantity(int quantity) {
		validateQuantity(quantity);

		this.quantity += quantity;

		if (isCategoryItem()) {
			uncheckCategoryItem();
			return;
		}

		updateScannedAt();
	}

	public void changeQuantity(int quantity) {
		validateQuantity(quantity);

		if (isCategoryItem()) {
			this.quantity = quantity;
			uncheckCategoryItem();
			return;
		}

		if (quantity < this.scannedQuantity) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}

		this.quantity = quantity;
		updateScannedAt();
	}

	public void changeChecked(boolean checked) {
		if (isCategoryItem()) {
			this.checked = checked;
			this.scannedAt = checked ? LocalDateTime.now() : null;
			return;
		}

		if (checked) {
			this.scannedQuantity = this.quantity;
		} else {
			this.scannedQuantity = 0;
		}

		updateScannedAt();
	}

	public void decreaseQuantityByScan(int quantity) {
		validateProductItem();
		validateQuantity(quantity);

		if (quantity > this.scannedQuantity) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}

		if (this.quantity - quantity < 1) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}

		this.quantity -= quantity;
		this.scannedQuantity -= quantity;

		updateScannedAt();
	}

	private void updateScannedAt() {
		if (isCategoryItem()) {
			this.scannedAt = this.checked ? LocalDateTime.now() : null;
			return;
		}

		if (this.scannedQuantity == this.quantity) {
			this.checked = true;
			this.scannedAt = LocalDateTime.now();
			return;
		}

		this.checked = false;
		this.scannedAt = null;
	}

	private void uncheckCategoryItem() {
		this.checked = false;
		this.scannedAt = null;
	}

	private void validateProductItem() {
		if (!isProductItem()) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_ITEM_TYPE);
		}
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}
	}

	private void validateItemTarget(
		ShoppingListItemType itemType,
		Long productId,
		String categoryName
	) {
		if (itemType == null) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_ITEM_TYPE);
		}

		if (itemType == ShoppingListItemType.PRODUCT) {
			validateProductTarget(productId);
			return;
		}

		validateCategoryTarget(productId, categoryName);
	}

	private void validateProductTarget(Long productId) {
		if (productId == null) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_ITEM_TYPE);
		}
	}

	private void validateCategoryTarget(Long productId, String categoryName) {
		if (productId != null) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_ITEM_TYPE);
		}

		if (categoryName == null || categoryName.isBlank()) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_CATEGORY);
		}
	}

	private void validateScannedQuantity(
		ShoppingListItemType itemType,
		int quantity,
		int scannedQuantity
	) {
		if (scannedQuantity < 0 || scannedQuantity > quantity) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}

		if (itemType == ShoppingListItemType.CATEGORY && scannedQuantity != 0) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}
	}

	private boolean resolveChecked(
		ShoppingListItemType itemType,
		int quantity,
		int scannedQuantity,
		boolean checked
	) {
		if (itemType == ShoppingListItemType.PRODUCT) {
			return scannedQuantity == quantity;
		}

		return checked;
	}

	private static String normalizeCategoryName(String categoryName) {
		if (categoryName == null) {
			return null;
		}

		return categoryName.trim();
	}

	public boolean requiresScanToDecrease(int newQuantity) {
		validateQuantity(newQuantity);

		if (!isProductItem()) {
			return false;
		}

		return newQuantity < this.scannedQuantity;
	}
}