package com.princesses7.findy.shopping.cart.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.analytics.event.RecommendationSource;
import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
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
	private final CartStockSynchronizer cartStockSynchronizer;
	private final ShoppingAnalyticsEventService shoppingAnalyticsEventService;

	@Transactional
	public CartResponse addCartItem(Long userId, AddCartItemRequest request, long storeId) {
		Cart cart = getOrCreateCart(userId);
		int quantity = request.quantityOrDefault();

		int targetQuantity = getCartItemQuantity(cart, request.productId()) + quantity;
		productSummaryReader.validatePurchasable(request.productId(), targetQuantity, storeId);

		cart.addItem(request.productId(), quantity);

		shoppingAnalyticsEventService.publishCartItemAdded(
			userId,
			request.productId(),
			quantity,
			RecommendationSource.DIRECT,
			null
		);

		return syncAndToResponse(cart, storeId);
	}

	@Transactional
	public CartResponse getCart(Long userId, long storeId) {
		return cartRepository.findByUserId(userId)
			.map(cart -> syncAndToResponse(cart, storeId))
			.orElseGet(() -> CartResponse.empty(userId));
	}

	@Transactional
	public CartResponse removeCartItem(Long userId, Long cartItemId, long storeId) {
		Cart cart = getCartByUserId(userId);

		cart.removeItem(cartItemId);

		return syncAndToResponse(cart, storeId);
	}

	@Transactional
	public CartResponse syncCartItemStocks(Long userId, long storeId) {
		return cartRepository.findByUserId(userId)
			.map(cart -> syncAndToResponse(cart, storeId))
			.orElseGet(() -> CartResponse.empty(userId));
	}

	@Transactional
	public CartResponse changeCartItemQuantity(
		Long userId,
		Long cartItemId,
		ChangeCartItemQuantityRequest request,
		long storeId
	) {
		Cart cart = getCartByUserId(userId);
		CartItem cartItem = getCartItem(cart, cartItemId);

		productSummaryReader.validatePurchasable(cartItem.getProductId(), request.quantity(), storeId);
		cart.changeItemQuantity(cartItemId, request.quantity());

		return syncAndToResponse(cart, storeId);
	}

	@Transactional
	public CartResponse changeCartItemChecked(
		Long userId,
		Long cartItemId,
		ChangeCartItemCheckedRequest request,
		long storeId
	) {
		Cart cart = getCartByUserId(userId);
		CartItem cartItem = getCartItem(cart, cartItemId);

		if (request.checked()) {
			productSummaryReader.validatePurchasable(
				cartItem.getProductId(),
				cartItem.getQuantity(),
				storeId
			);
		}

		cart.changeItemChecked(cartItemId, request.checked());

		return syncAndToResponse(cart, storeId);
	}

	private Cart getOrCreateCart(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseGet(() -> cartRepository.save(Cart.create(userId)));
	}

	public Cart getCartByUserId(Long userId) {
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

	private CartResponse syncAndToResponse(Cart cart, long storeId) {
		Map<Long, ProductSummaryResponse> productMap = getProductSummaryMap(cart, storeId);

		cartStockSynchronizer.synchronize(cart, productMap);

		return CartResponse.from(cart, productMap);
	}

	private Map<Long, ProductSummaryResponse> getProductSummaryMap(Cart cart, long storeId) {
		List<Long> productIds = cart.getCartItems().stream()
			.map(CartItem::getProductId)
			.toList();

		return productSummaryReader.getProductSummaryMap(productIds, storeId);
	}
}