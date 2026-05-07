package com.princesses7.findy.shopping.shoppinglist.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shopping_lists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopping_list_id")
	private Long shoppingListId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ShoppingListItem> shoppingListItems = new ArrayList<>();

	private ShoppingList(Cart cart) {
		this.cart = cart;
	}

	public static ShoppingList create(Cart cart) {
		List<CartItem> checkedItems = cart.getCheckedItems();

		if (checkedItems.isEmpty()) {
			throw new ShoppingListException(EMPTY_SHOPPING_LIST);
		}

		ShoppingList shoppingList = new ShoppingList(cart);

		checkedItems.forEach(cartItem ->
			shoppingList.shoppingListItems.add(
				ShoppingListItem.createFromCartItem(shoppingList, cartItem)
			)
		);

		return shoppingList;
	}

	public void addDirectItem(Long productId, int quantity) {
		validateQuantity(quantity);

		findItemByProductId(productId)
			.ifPresentOrElse(
				item -> item.increaseQuantity(quantity),
				() -> shoppingListItems.add(
					ShoppingListItem.createDirectItem(this, productId, quantity)
				)
			);
	}

	public void completeScan(Long productId) {
		ShoppingListItem item = getItemByProductId(productId);
		item.completeScan();
	}

	public void removeItem(Long shoppingListItemId) {
		ShoppingListItem item = getShoppingListItem(shoppingListItemId);
		shoppingListItems.remove(item);
	}

	public int getTotalItemCount() {
		return shoppingListItems.size();
	}

	public long getScannedItemCount() {
		return shoppingListItems.stream()
			.filter(ShoppingListItem::isScanned)
			.count();
	}

	public boolean hasScannedItem() {
		return shoppingListItems.stream()
			.anyMatch(ShoppingListItem::isScanned);
	}

	private Optional<ShoppingListItem> findItemByProductId(Long productId) {
		return shoppingListItems.stream()
			.filter(item -> item.hasSameProduct(productId))
			.findFirst();
	}

	private ShoppingListItem getItemByProductId(Long productId) {
		return findItemByProductId(productId)
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_ITEM_NOT_FOUND));
	}

	private ShoppingListItem getShoppingListItem(Long shoppingListItemId) {
		return shoppingListItems.stream()
			.filter(item -> item.hasSameId(shoppingListItemId))
			.findFirst()
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_ITEM_NOT_FOUND));
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_QUANTITY);
		}
	}
}