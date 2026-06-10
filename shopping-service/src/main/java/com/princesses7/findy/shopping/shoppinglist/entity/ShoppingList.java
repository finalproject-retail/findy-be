package com.princesses7.findy.shopping.shoppinglist.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "shopping_lists")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingList extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopping_list_id")
	private Long shoppingListId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false, unique = true)
	private Cart cart;

	@OneToMany(mappedBy = "shoppingList", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ShoppingListItem> shoppingListItems = new ArrayList<>();

	private ShoppingList(Long userId, Cart cart) {
		this.userId = userId;
		this.cart = cart;
	}

	public static ShoppingList create(Cart cart) {
		ShoppingList shoppingList = createEmpty(cart);
		shoppingList.replaceItemsFromCart();

		return shoppingList;
	}

	public static ShoppingList createEmpty(Cart cart) {
		if (cart.hasShoppingList()) {
			throw new ShoppingListException(SHOPPING_LIST_ALREADY_EXISTS);
		}

		ShoppingList shoppingList = new ShoppingList(cart.getUserId(), cart);
		cart.assignShoppingList(shoppingList);

		return shoppingList;
	}

	public static ShoppingList createFromCart(Cart cart) {
		return create(cart);
	}

	public void replaceItemsFromCart() {
		List<CartItem> checkedItems = cart.getCheckedItems();

		if (checkedItems.isEmpty()) {
			throw new ShoppingListException(EMPTY_SHOPPING_LIST);
		}

		shoppingListItems.clear();

		checkedItems.forEach(cartItem ->
			shoppingListItems.add(
				ShoppingListItem.createFromCartItem(this, cartItem)
			)
		);
	}

	public void mergeSelectedCartItems(List<CartItem> cartItems) {
		cartItems.forEach(this::mergeSelectedCartItem);
	}

	private void mergeSelectedCartItem(CartItem cartItem) {
		findItemByProductId(cartItem.getProductId())
			.ifPresentOrElse(
				item -> increaseOnlyWhenCartQuantityIsGreater(item, cartItem),
				() -> shoppingListItems.add(
					ShoppingListItem.createFromCartItem(this, cartItem)
				)
			);
	}

	private void increaseOnlyWhenCartQuantityIsGreater(
		ShoppingListItem item,
		CartItem cartItem
	) {
		int additionalQuantity = cartItem.getQuantity() - item.getQuantity();

		if (additionalQuantity <= 0) {
			return;
		}

		item.increaseQuantity(additionalQuantity);
	}

	public void addSearchedItem(Long productId, int quantity) {
		validateQuantity(quantity);

		findItemByProductId(productId)
			.ifPresentOrElse(
				item -> item.increaseQuantity(quantity),
				() -> shoppingListItems.add(
					ShoppingListItem.createFromSearch(this, productId, quantity)
				)
			);
	}

	public void addScannedItem(Long productId, int quantity) {
		validateQuantity(quantity);

		findItemByProductId(productId)
			.ifPresentOrElse(
				item -> item.scanOrIncreaseQuantity(quantity),
				() -> shoppingListItems.add(
					ShoppingListItem.createFromScan(
						this,
						productId,
						quantity
					)
				)
			);
	}

	public void addCategoryItem(
		Long categoryId,
		String categoryName,
		int quantity
	) {
		validateQuantity(quantity);
		validateCategoryName(categoryName);

		findItemByCategory(categoryId, categoryName)
			.ifPresentOrElse(
				item -> item.increaseQuantity(quantity),
				() -> shoppingListItems.add(
					ShoppingListItem.createFromCategory(
						this,
						categoryId,
						categoryName,
						quantity
					)
				)
			);
	}

	public void changeItemChecked(
		Long shoppingListItemId,
		boolean checked
	) {
		ShoppingListItem item = getShoppingListItem(shoppingListItemId);

		item.changeChecked(checked);
	}

	public void completeScan(Long productId) {
		ShoppingListItem item = getItemByProductId(productId);

		item.completeScan();
	}

	public void changeItemQuantity(
		Long shoppingListItemId,
		int quantity
	) {
		ShoppingListItem item = getShoppingListItem(shoppingListItemId);

		item.changeQuantity(quantity);
	}

	public void decreaseQuantityByScan(
		Long productId,
		int quantity
	) {
		validateQuantity(quantity);

		ShoppingListItem item = getItemByProductId(productId);

		if (item.hasQuantity(quantity)) {
			shoppingListItems.remove(item);
			return;
		}

		item.decreaseQuantityByScan(quantity);
	}

	public void removeItem(Long shoppingListItemId) {
		ShoppingListItem item = getShoppingListItem(shoppingListItemId);

		shoppingListItems.remove(item);
	}

	public void returnItemToCart(Long shoppingListItemId) {
		ShoppingListItem item = getShoppingListItem(shoppingListItemId);

		if (item.isProductItem()) {
			cart.saveItemForNextShopping(
				item.getProductId(),
				item.getQuantity()
			);
		}

		shoppingListItems.remove(item);
	}

	public void cancel() {
		cart.uncheckAllItems();
		cart.clearShoppingList();
	}

	public int getTotalItemCount() {
		return shoppingListItems.stream()
			.mapToInt(ShoppingListItem::getQuantity)
			.sum();
	}

	public long getScannedItemCount() {
		return shoppingListItems.stream()
			.mapToInt(ShoppingListItem::getCompletedQuantity)
			.sum();
	}

	public void moveProductItemsToCartForNextShopping() {
		shoppingListItems.stream()
			.filter(ShoppingListItem::isProductItem)
			.forEach(item ->
				cart.saveItemForNextShopping(
					item.getProductId(),
					item.getQuantity()
				)
			);
	}

	private Optional<ShoppingListItem> findItemByProductId(Long productId) {
		return shoppingListItems.stream()
			.filter(item -> item.hasSameProduct(productId))
			.findFirst();
	}

	private Optional<ShoppingListItem> findItemByCategory(
		Long categoryId,
		String categoryName
	) {
		return shoppingListItems.stream()
			.filter(item -> item.hasSameCategory(categoryId, categoryName))
			.findFirst();
	}

	private ShoppingListItem getItemByProductId(Long productId) {
		return findItemByProductId(productId)
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_ITEM_NOT_FOUND));
	}

	public ShoppingListItem getShoppingListItem(Long shoppingListItemId) {
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

	private void validateCategoryName(String categoryName) {
		if (categoryName == null || categoryName.isBlank()) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_CATEGORY);
		}
	}
}