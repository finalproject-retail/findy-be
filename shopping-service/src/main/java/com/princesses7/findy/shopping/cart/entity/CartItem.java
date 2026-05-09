package com.princesses7.findy.shopping.cart.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

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
@Table(name = "cart_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cart_item_id")
	private Long cartItemId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cart_id", nullable = false)
	private Cart cart;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "quantity", nullable = false)
	private int quantity;

	@Column(name = "is_checked", nullable = false)
	private boolean checked = false;

	private CartItem(Cart cart, Long productId, int quantity) {
		validateQuantity(quantity);
		this.cart = cart;
		this.productId = productId;
		this.quantity = quantity;
		this.checked = false;
	}

	public static CartItem create(Cart cart, Long productId, int quantity) {
		return new CartItem(cart, productId, quantity);
	}

	public boolean hasSameId(Long cartItemId) {
		return this.cartItemId != null && this.cartItemId.equals(cartItemId);
	}

	public boolean hasSameProduct(Long productId) {
		return this.productId.equals(productId);
	}

	public void increaseQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity += quantity;
	}

	public void changeQuantity(int quantity) {
		validateQuantity(quantity);
		this.quantity = quantity;
	}

	public void changeChecked(boolean checked) {
		this.checked = checked;
	}

	private void validateQuantity(int quantity) {
		if (quantity < 1) {
			throw new CartException(INVALID_CART_QUANTITY);
		}
	}
}