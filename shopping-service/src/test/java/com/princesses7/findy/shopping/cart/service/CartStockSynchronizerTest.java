package com.princesses7.findy.shopping.cart.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

class CartStockSynchronizerTest {

	private final CartStockSynchronizer synchronizer = new CartStockSynchronizer();

	@Test
	@DisplayName("재고보다 장바구니 수량이 많으면 구매 가능 수량으로 보정한다")
	void synchronizeClampsQuantityWhenQuantityExceedsStock() {
		Cart cart = Cart.create(1L);
		cart.addItem(100L, 3);
		CartItem cartItem = cart.getCartItems().get(0);
		cartItem.changeChecked(true);

		synchronizer.synchronize(cart, Map.of(
			100L,
			product(100L, SaleStatus.ON_SALE, 1)
		));

		assertThat(cartItem.getQuantity()).isEqualTo(1);
		assertThat(cartItem.isChecked()).isTrue();
	}

	@Test
	@DisplayName("재고가 없으면 수량은 유지하고 선택 상태만 해제한다")
	void synchronizeUnchecksOutOfStockItem() {
		Cart cart = Cart.create(1L);
		cart.addItem(100L, 3);
		CartItem cartItem = cart.getCartItems().get(0);
		cartItem.changeChecked(true);

		synchronizer.synchronize(cart, Map.of(
			100L,
			product(100L, SaleStatus.ON_SALE, 0)
		));

		assertThat(cartItem.getQuantity()).isEqualTo(3);
		assertThat(cartItem.isChecked()).isFalse();
	}

	@Test
	@DisplayName("재고 정보가 없으면 선택 상태를 해제한다")
	void synchronizeUnchecksNoInventoryItem() {
		Cart cart = Cart.create(1L);
		cart.addItem(100L, 3);
		CartItem cartItem = cart.getCartItems().get(0);
		cartItem.changeChecked(true);

		synchronizer.synchronize(cart, Map.of(
			100L,
			product(100L, SaleStatus.ON_SALE, null)
		));

		assertThat(cartItem.getQuantity()).isEqualTo(3);
		assertThat(cartItem.isChecked()).isFalse();
	}

	@Test
	@DisplayName("판매 상태가 판매중이 아니면 선택 상태를 해제한다")
	void synchronizeUnchecksNotOnSaleItem() {
		Cart cart = Cart.create(1L);
		cart.addItem(100L, 3);
		CartItem cartItem = cart.getCartItems().get(0);
		cartItem.changeChecked(true);

		synchronizer.synchronize(cart, Map.of(
			100L,
			product(100L, SaleStatus.OUT_OF_STOCK, 20)
		));

		assertThat(cartItem.getQuantity()).isEqualTo(3);
		assertThat(cartItem.isChecked()).isFalse();
	}

	@Test
	@DisplayName("재고가 충분하면 수량과 선택 상태를 유지한다")
	void synchronizeKeepsPurchasableItem() {
		Cart cart = Cart.create(1L);
		cart.addItem(100L, 3);
		CartItem cartItem = cart.getCartItems().get(0);
		cartItem.changeChecked(true);

		synchronizer.synchronize(cart, Map.of(
			100L,
			product(100L, SaleStatus.ON_SALE, 3)
		));

		assertThat(cartItem.getQuantity()).isEqualTo(3);
		assertThat(cartItem.isChecked()).isTrue();
	}

	private ProductSummaryResponse product(Long productId, SaleStatus saleStatus, Integer stockQuantity) {
		return new ProductSummaryResponse(
			productId,
			"브랜드",
			"상품",
			"barcode",
			null,
			1000,
			saleStatus,
			1L,
			stockQuantity,
			stockQuantity == null ? null : "IN_STOCK",
			stockQuantity == null ? "재고 확인 불가" : "남은 재고 " + stockQuantity + "개"
		);
	}
}