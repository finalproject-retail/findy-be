package com.princesses7.findy.shopping.cart.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.princesses7.findy.shopping.cart.exception.CartException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_id")
	private Long cartId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartItem> cartItems = new ArrayList<>();

	private Cart(Long userId) {
		this.userId = userId;
	}

	public static Cart create(Long userId) {
		return new Cart(userId);
	}

	public void addItem(Long productId, int quantity) {
		validateQuantity(quantity);

		findItemByProductId(productId)
			.ifPresentOrElse(
				cartItem -> cartItem.increaseQuantity(quantity),
				() -> cartItems.add(CartItem.create(this, productId, quantity))
			);
	}

	public void removeItem(Long cartItemId) {
		CartItem cartItem = getCartItem(cartItemId);
		cartItems.remove(cartItem);
	}

	public void changeItemQuantity(Long cartItemId, int quantity) {
		validateQuantity(quantity);

		CartItem cartItem = getCartItem(cartItemId);
		cartItem.changeQuantity(quantity);
	}

	public void changeItemChecked(Long cartItemId, boolean checked) {
		CartItem cartItem = getCartItem(cartItemId);
		cartItem.changeChecked(checked);
	}

	public List<CartItem> getCheckedItems() {
		return cartItems.stream()
			.filter(CartItem::isChecked)
			.toList();
	}

	public boolean hasCheckedItem() {
		return cartItems.stream()
			.anyMatch(CartItem::isChecked);
	}

	private Optional<CartItem> findItemByProductId(Long productId) {
		return cartItems.stream()
			.filter(cartItem -> cartItem.hasSameProduct(productId))
			.findFirst();
	}

	private CartItem getCartItem(Long cartItemId) {
		return cartItems.stream()
			.filter(cartItem -> cartItem.hasSameId(cartItemId))
			.findFirst()
			.orElseThrow(() -> new CartException(CART_ITEM_NOT_FOUND));
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new CartException(INVALID_CART_QUANTITY);
		}
	}
}