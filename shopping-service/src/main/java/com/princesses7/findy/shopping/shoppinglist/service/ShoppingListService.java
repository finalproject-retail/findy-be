package com.princesses7.findy.shopping.shoppinglist.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.cart.repository.CartRepository;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.service.ProductSummaryReader;
import com.princesses7.findy.shopping.shoppinglist.dto.request.AddShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ChangeShoppingListItemQuantityRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ScanShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.response.ShoppingListResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.repository.ShoppingListRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

	private final CartRepository cartRepository;
	private final ShoppingListRepository shoppingListRepository;
	private final ProductSummaryReader productSummaryReader;

	@Transactional
	public ShoppingListResponse createShoppingList(Long userId) {
		Cart cart = getCartByUserId(userId);

		ShoppingList shoppingList = ShoppingList.create(cart);
		ShoppingList savedShoppingList = shoppingListRepository.save(shoppingList);

		return toResponse(savedShoppingList);
	}

	public ShoppingListResponse getShoppingList(Long userId) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse addShoppingListItem(
		Long userId,
		AddShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.addSearchedItem(
			request.productId(),
			request.quantityOrDefault()
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse scanShoppingListItem(
		Long userId,
		ScanShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.addScannedItem(
			request.productId(),
			request.quantityOrDefault()
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse decreaseShoppingListItemQuantityByScan(
		Long userId,
		ScanShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.decreaseQuantityByScan(
			request.productId(),
			request.quantityOrDefault()
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemQuantity(
		Long userId,
		Long shoppingListItemId,
		ChangeShoppingListItemQuantityRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.changeItemQuantity(
			shoppingListItemId,
			request.quantity()
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse removeShoppingListItem(
		Long userId,
		Long shoppingListItemId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.removeItem(shoppingListItemId);

		return toResponse(shoppingList);
	}

	@Transactional
	public void cancelShopping(Long userId) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.cancel();
		shoppingListRepository.delete(shoppingList);
	}

	private Cart getCartByUserId(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseThrow(() -> new CartException(CART_NOT_FOUND));
	}

	private ShoppingList getShoppingListByUserId(Long userId) {
		return shoppingListRepository.findByUserId(userId)
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_NOT_FOUND));
	}

	private ShoppingListResponse toResponse(ShoppingList shoppingList) {
		List<Long> productIds = shoppingList.getShoppingListItems().stream()
			.map(ShoppingListItem::getProductId)
			.toList();

		Map<Long, ProductSummaryResponse> productMap = productSummaryReader
			.getProductSummaryMap(productIds);

		return ShoppingListResponse.from(shoppingList, productMap);
	}
}