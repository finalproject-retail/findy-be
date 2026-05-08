package com.princesses7.findy.shopping.cart.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.dto.request.AddCartItemRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemCheckedRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemQuantityRequest;
import com.princesses7.findy.shopping.cart.dto.response.CartResponse;
import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

	private final CartRepository cartRepository;

	@Transactional
	public CartResponse addCartItem(Long userId, AddCartItemRequest request) {
		Cart cart = getOrCreateCart(userId);

		// TODO: Product Service 연동 후 상품 존재 여부, 품절 여부, 재고 수량 검증 추가
		cart.addItem(request.productId(), request.quantity());

		Cart savedCart = cartRepository.saveAndFlush(cart);

		return CartResponse.from(savedCart);
	}

	public CartResponse getCart(Long userId) {
		return cartRepository.findByUserId(userId)
			.map(CartResponse::from)
			.orElseGet(() -> CartResponse.empty(userId));
	}

	@Transactional
	public CartResponse removeCartItem(Long userId, Long cartItemId) {
		Cart cart = getCartByUserId(userId);

		cart.removeItem(cartItemId);

		Cart savedCart = cartRepository.saveAndFlush(cart);

		return CartResponse.from(savedCart);
	}

	@Transactional
	public CartResponse changeCartItemQuantity(
		Long userId,
		Long cartItemId,
		ChangeCartItemQuantityRequest request
	) {
		Cart cart = getCartByUserId(userId);

		// TODO: Product Service 연동 후 재고 수량 초과 여부 검증 추가
		cart.changeItemQuantity(cartItemId, request.quantity());

		Cart savedCart = cartRepository.saveAndFlush(cart);

		return CartResponse.from(savedCart);
	}

	@Transactional
	public CartResponse changeCartItemChecked(
		Long userId,
		Long cartItemId,
		ChangeCartItemCheckedRequest request
	) {
		Cart cart = getCartByUserId(userId);

		cart.changeItemChecked(cartItemId, request.checked());

		Cart savedCart = cartRepository.saveAndFlush(cart);

		return CartResponse.from(savedCart);
	}

	private Cart getOrCreateCart(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseGet(() -> cartRepository.save(Cart.create(userId)));
	}

	private Cart getCartByUserId(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseThrow(() -> new CartException(CART_NOT_FOUND));
	}
}