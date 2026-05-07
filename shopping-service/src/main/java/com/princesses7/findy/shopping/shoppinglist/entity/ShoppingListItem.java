package com.princesses7.findy.shopping.shoppinglist.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.type.EntryType;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;

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
public class ShoppingListItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopping_list_item_id")
	private Long shoppingListItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shopping_list_id", nullable = false)
	private ShoppingList shoppingList;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "scan_status", nullable = false)
	private ScanStatus scanStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "entry_type", nullable = false)
	private EntryType entryType;

	private ShoppingListItem(
		ShoppingList shoppingList,
		Long productId,
		CartItem cartItem,
		int quantity,
		ScanStatus scanStatus,
		EntryType entryType
	) {
		validateQuantity(quantity);
		this.shoppingList = shoppingList;
		this.productId = productId;
		this.cartItem = cartItem;
		this.quantity = quantity;
		this.scanStatus = scanStatus;
		this.entryType = entryType;
	}

	public static ShoppingListItem createFromCartItem(ShoppingList shoppingList, CartItem cartItem) {
		return new ShoppingListItem(
			shoppingList,
			cartItem.getProductId(),
			cartItem,
			cartItem.getQuantity(),
			ScanStatus.NOT_SCANNED,
			EntryType.CART
		);
	}

	public static ShoppingListItem createDirectItem(ShoppingList shoppingList, Long productId, int quantity) {
		return new ShoppingListItem(
			shoppingList,
			productId,
			null,
			quantity,
			ScanStatus.SCANNED,
			EntryType.DIRECT
		);
	}

	public boolean hasSameId(Long shoppingListItemId) {
		return this.shoppingListItemId != null && this.shoppingListItemId.equals(shoppingListItemId);
	}

	public boolean hasSameProduct(Long productId) {
		return this.productId.equals(productId);
	}

	public boolean isScanned() {
		return this.scanStatus == ScanStatus.SCANNED;
	}

	public boolean isFromCart() {
		return this.entryType == EntryType.CART;
	}

	public boolean isDirect() {
		return this.entryType == EntryType.DIRECT;
	}

	public void completeScan() {
		if (isScanned()) {
			throw new ShoppingListException(ALREADY_SCANNED_ITEM);
		}

		this.scanStatus = ScanStatus.SCANNED;
	}

	public void increaseQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity += quantity;
	}

	public void changeQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity = quantity;
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}
	}
}