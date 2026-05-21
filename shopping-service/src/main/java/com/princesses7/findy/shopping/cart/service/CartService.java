package com.princesses7.findy.shopping.cart.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.dto.request.AddCartItemRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemCheckedRequest;
import com.princesses7.findy.shopping.cart.dto.request.ChangeCartItemQuantityRequest;
import com.princesses7.findy.shopping.cart.dto.response.CartResponse;
import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.entity.CartItem;
import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.cart.repository.CartRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.service.ProductSummaryReader;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

	private final CartRepository cartRepository;
	private final ProductSummaryReader productSummaryReader;

	@Transactional
	public CartResponse addCartItem(Long userId, AddCartItemRequest request) {
		Cart cart = getOrCreateCart(userId);
		int quantity = request.quantityOrDefault();
		int targetQuantity = getCartItemQuantity(cart, request.productId()) + quantity;

		productSummaryReader.validatePurchasable(request.productId(), targetQuantity);
		cart.addItem(request.productId(), quantity);

		return toResponse(cart);
	}

	public CartResponse getCart(Long userId) {
		return cartRepository.findByUserId(userId)
			.map(this::toResponse)
			.orElseGet(() -> CartResponse.empty(userId));
	}

	@Transactional
	public CartResponse removeCartItem(Long userId, Long cartItemId) {
		Cart cart = getCartByUserId(userId);

		cart.removeItem(cartItemId);

		return toResponse(cart);
	}

	@Transactional
	public CartResponse changeCartItemQuantity(
		Long userId,
		Long cartItemId,
		ChangeCartItemQuantityRequest request
	) {
		Cart cart = getCartByUserId(userId);
		CartItem cartItem = getCartItem(cart, cartItemId);

		productSummaryReader.validatePurchasable(cartItem.getProductId(), request.quantity());
		cart.changeItemQuantity(cartItemId, request.quantity());

		return toResponse(cart);
	}

	@Transactional
	public CartResponse changeCartItemChecked(
		Long userId,
		Long cartItemId,
		ChangeCartItemCheckedRequest request
	) {
		Cart cart = getCartByUserId(userId);

		cart.changeItemChecked(cartItemId, request.checked());

		return toResponse(cart);
	}

	private Cart getOrCreateCart(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseGet(() -> cartRepository.save(Cart.create(userId)));
	}

	private Cart getCartByUserId(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseThrow(() -> new CartException(CART_NOT_FOUND));
	}

	private CartItem getCartItem(Cart cart, Long cartItemId) {
		return cart.getCartItems().stream()
			.filter(cartItem -> cartItem.hasSameId(cartItemId))
			.findFirst()
			.orElseThrow(() -> new CartException(CART_ITEM_NOT_FOUND));
	}

	private int getCartItemQuantity(Cart cart, Long productId) {
		return cart.getCartItems().stream()
			.filter(cartItem -> cartItem.hasSameProduct(productId))
			.mapToInt(CartItem::getQuantity)
			.findFirst()
			.orElse(0);
	}

	private CartResponse toResponse(Cart cart) {
		List<Long> productIds = cart.getCartItems().stream()
			.map(CartItem::getProductId)
			.toList();

		Map<Long, ProductSummaryResponse> productMap = productSummaryReader
			.getProductSummaryMap(productIds);

		return CartResponse.from(cart, productMap);
	}
}