package com.princesses7.findy.shopping.shoppinglist.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_item_id")
	private CartItem cartItem;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "scanned_at")
	private LocalDateTime scannedAt;

	private ShoppingListItem(
		ShoppingList shoppingList,
		Long productId,
		CartItem cartItem,
		int quantity,
		LocalDateTime scannedAt
	) {
		validateQuantity(quantity);
		this.shoppingList = shoppingList;
		this.productId = productId;
		this.cartItem = cartItem;
		this.quantity = quantity;
		this.scannedAt = scannedAt;
	}

	public static ShoppingListItem createFromCartItem(
		ShoppingList shoppingList,
		CartItem cartItem
	) {
		return new ShoppingListItem(
			shoppingList,
			cartItem.getProductId(),
			cartItem,
			cartItem.getQuantity(),
			null
		);
	}

	public static ShoppingListItem createUnscannedItem(
		ShoppingList shoppingList,
		Long productId,
		int quantity
	) {
		return new ShoppingListItem(
			shoppingList,
			productId,
			null,
			quantity,
			null
		);
	}

	public static ShoppingListItem createScannedItem(
		ShoppingList shoppingList,
		Long productId,
		int quantity
	) {
		return new ShoppingListItem(
			shoppingList,
			productId,
			null,
			quantity,
			LocalDateTime.now()
		);
	}

	public boolean hasSameId(Long shoppingListItemId) {
		return this.shoppingListItemId != null
			&& this.shoppingListItemId.equals(shoppingListItemId);
	}

	public boolean hasSameProduct(Long productId) {
		return this.productId.equals(productId);
	}

	public boolean isScanned() {
		return this.scannedAt != null;
	}

	public void completeScan() {
		if (isScanned()) {
			return;
		}

		this.scannedAt = LocalDateTime.now();
	}

	public void increaseQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity += quantity;
		this.scannedAt = null;
	}

	public void changeQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity = quantity;
		this.scannedAt = null;
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}
	}

	public void scanOrIncreaseQuantity(int quantity) {
		validateQuantity(quantity);

		if (isScanned()) {
			this.quantity += quantity;
			return;
		}

		this.scannedAt = LocalDateTime.now();
	}
}