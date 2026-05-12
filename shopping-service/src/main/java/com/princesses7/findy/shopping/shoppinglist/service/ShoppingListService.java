package com.princesses7.findy.shopping.shoppinglist.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.cart.repository.CartRepository;
import com.princesses7.findy.shopping.shoppinglist.dto.request.AddShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ChangeShoppingListItemQuantityRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ScanShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.response.ShoppingListResponse;
import com.princesses7.findy.shopping.shoppinglist.dto.response.ShoppingListSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.repository.ShoppingListRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

	private final CartRepository cartRepository;
	private final ShoppingListRepository shoppingListRepository;

	@Transactional
	public ShoppingListResponse createShoppingList(Long userId) {
		Cart cart = getCartByUserId(userId);

		ShoppingList shoppingList = ShoppingList.create(cart);
		ShoppingList savedShoppingList = shoppingListRepository.save(shoppingList);

		return ShoppingListResponse.from(savedShoppingList);
	}

	public List<ShoppingListSummaryResponse> getShoppingLists(Long userId) {
		return shoppingListRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
			.map(ShoppingListSummaryResponse::from)
			.toList();
	}

	public ShoppingListResponse getShoppingList(Long userId, Long shoppingListId) {
		ShoppingList shoppingList = getShoppingListByIdAndUserId(userId, shoppingListId);

		return ShoppingListResponse.from(shoppingList);
	}

	@Transactional
	public ShoppingListResponse addShoppingListItem(
		Long userId,
		Long shoppingListId,
		AddShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByIdAndUserId(userId, shoppingListId);

		// TODO: Product Service 연동 후 상품 존재 여부, 품절 여부 검증 추가
		shoppingList.addUnscannedItem(request.productId(), request.quantity());

		return ShoppingListResponse.from(shoppingList);
	}

	@Transactional
	public ShoppingListResponse scanShoppingListItem(
		Long userId,
		Long shoppingListId,
		ScanShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByIdAndUserId(userId, shoppingListId);

		// TODO: Product Service 연동 후 barcode -> productId 매칭으로 변경
		// 현재는 productId 기준으로 먼저 구현
		shoppingList.addScannedItem(request.productId(), request.quantityOrDefault());

		return ShoppingListResponse.from(shoppingList);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemQuantity(
		Long userId,
		Long shoppingListId,
		Long shoppingListItemId,
		ChangeShoppingListItemQuantityRequest request
	) {
		ShoppingList shoppingList = getShoppingListByIdAndUserId(userId, shoppingListId);

		shoppingList.getShoppingListItems().stream()
			.filter(item -> item.hasSameId(shoppingListItemId))
			.findFirst()
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_ITEM_NOT_FOUND))
			.changeQuantity(request.quantity());

		return ShoppingListResponse.from(shoppingList);
	}

	@Transactional
	public ShoppingListResponse removeShoppingListItem(
		Long userId,
		Long shoppingListId,
		Long shoppingListItemId
	) {
		ShoppingList shoppingList = getShoppingListByIdAndUserId(userId, shoppingListId);

		shoppingList.removeItem(shoppingListItemId);

		return ShoppingListResponse.from(shoppingList);
	}

	private Cart getCartByUserId(Long userId) {
		return cartRepository.findByUserId(userId)
			.orElseThrow(() -> new CartException(CART_NOT_FOUND));
	}

	private ShoppingList getShoppingListByIdAndUserId(Long userId, Long shoppingListId) {
		return shoppingListRepository.findByShoppingListIdAndUserId(shoppingListId, userId)
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_NOT_FOUND));
	}
}