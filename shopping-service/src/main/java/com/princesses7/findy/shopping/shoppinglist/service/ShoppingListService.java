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
import com.princesses7.findy.shopping.product.service.ProductBarcodeReader;
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
	private final ProductBarcodeReader productBarcodeReader;

	@Transactional
	public ShoppingListResponse createShoppingList(Long userId) {
		Cart cart = getCartByUserId(userId);

		validateCheckedItemsPurchasable(cart);

		ShoppingList shoppingList = shoppingListRepository.findByUserId(userId)
			.map(existingShoppingList -> {
				existingShoppingList.replaceItemsFromCart();
				return existingShoppingList;
			})
			.orElseGet(() -> shoppingListRepository.save(ShoppingList.create(cart)));

		return toResponse(shoppingList);
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
		int quantity = request.quantityOrDefault();
		int targetQuantity = getShoppingListItemQuantity(shoppingList, request.productId())
			+ quantity;

		productSummaryReader.validatePurchasable(request.productId(), targetQuantity);
		shoppingList.addSearchedItem(
			request.productId(),
			quantity
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse scanShoppingListItem(
		Long userId,
		ScanShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());
		int quantity = request.quantityOrDefault();
		int targetQuantity = calculateScannedTargetQuantity(
			shoppingList,
			productId,
			quantity
		);

		productSummaryReader.validatePurchasable(productId, targetQuantity);
		shoppingList.addScannedItem(
			productId,
			quantity
		);

		return toResponse(shoppingList);
	}

	@Transactional
	public ShoppingListResponse decreaseShoppingListItemQuantityByScan(
		Long userId,
		ScanShoppingListItemRequest request
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());

		shoppingList.decreaseQuantityByScan(
			productId,
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
		ShoppingListItem item = getShoppingListItem(shoppingList, shoppingListItemId);

		productSummaryReader.validatePurchasable(item.getProductId(), request.quantity());
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

	private void validateCheckedItemsPurchasable(Cart cart) {
		cart.getCheckedItems().forEach(cartItem ->
			productSummaryReader.validatePurchasable(
				cartItem.getProductId(),
				cartItem.getQuantity()
			)
		);
	}

	private ShoppingListItem getShoppingListItem(
		ShoppingList shoppingList,
		Long shoppingListItemId
	) {
		return shoppingList.getShoppingListItems().stream()
			.filter(item -> item.hasSameId(shoppingListItemId))
			.findFirst()
			.orElseThrow(() -> new ShoppingListException(SHOPPING_LIST_ITEM_NOT_FOUND));
	}

	private int getShoppingListItemQuantity(ShoppingList shoppingList, Long productId) {
		return shoppingList.getShoppingListItems().stream()
			.filter(item -> item.hasSameProduct(productId))
			.mapToInt(ShoppingListItem::getQuantity)
			.findFirst()
			.orElse(0);
	}

	private int calculateScannedTargetQuantity(
		ShoppingList shoppingList,
		Long productId,
		int scanQuantity
	) {
		return shoppingList.getShoppingListItems().stream()
			.filter(item -> item.hasSameProduct(productId))
			.findFirst()
			.map(item -> Math.max(
				item.getQuantity(),
				item.getScannedQuantity() + scanQuantity
			))
			.orElse(scanQuantity);
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