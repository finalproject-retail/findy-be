package com.princesses7.findy.shopping.shoppinglist.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.analytics.event.RecommendationSource;
import com.princesses7.findy.shopping.analytics.publisher.ShoppingAnalyticsEventService;
import com.princesses7.findy.shopping.cart.entity.Cart;
import com.princesses7.findy.shopping.cart.exception.CartException;
import com.princesses7.findy.shopping.cart.repository.CartRepository;
import com.princesses7.findy.shopping.category.entity.Category;
import com.princesses7.findy.shopping.category.repository.CategoryRepository;
import com.princesses7.findy.shopping.inventory.service.InventoryStockService;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.product.service.ProductBarcodeReader;
import com.princesses7.findy.shopping.product.service.ProductSummaryReader;
import com.princesses7.findy.shopping.shoppinglist.dto.request.AddCategoryShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.AddShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ChangeShoppingListItemCheckedRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ChangeShoppingListItemQuantityRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.request.ScanShoppingListItemRequest;
import com.princesses7.findy.shopping.shoppinglist.dto.response.ShoppingListResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.exception.ShoppingListException;
import com.princesses7.findy.shopping.shoppinglist.repository.ShoppingListRepository;
import com.princesses7.findy.shopping.store.StoreIdSupport;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingListService {

	private final InventoryStockService inventoryStockService;
	private final CartRepository cartRepository;
	private final ShoppingListRepository shoppingListRepository;
	private final ProductSummaryReader productSummaryReader;
	private final ProductBarcodeReader productBarcodeReader;
	private final ShoppingAnalyticsEventService shoppingAnalyticsEventService;
	private final CategoryRepository categoryRepository;

	@Transactional
	public ShoppingListResponse createShoppingList(Long userId, long storeId) {
		Cart cart = getCartByUserId(userId);

		validateCheckedItemsPurchasable(cart, storeId);

		ShoppingList shoppingList = ShoppingList.create(cart);
		ShoppingList savedShoppingList = shoppingListRepository.save(shoppingList);

		shoppingAnalyticsEventService.publishShoppingListItemsFromCart(
			userId,
			savedShoppingList,
			RecommendationSource.DIRECT
		);

		return toResponse(savedShoppingList, storeId);
	}

	public ShoppingListResponse getShoppingList(Long userId, long storeId) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse addShoppingListItem(
		Long userId,
		AddShoppingListItemRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		int quantity = request.quantityOrDefault();
		int targetQuantity = getShoppingListItemQuantity(shoppingList, request.productId())
			+ quantity;

		productSummaryReader.validatePurchasable(request.productId(), targetQuantity, storeId);

		shoppingList.addSearchedItem(
			request.productId(),
			quantity
		);

		shoppingAnalyticsEventService.publishShoppingListItemAdded(
			userId,
			List.of(request.productId()),
			RecommendationSource.fromNullable(request.recommendationSource()),
			request.originalProductId()
		);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse addCategoryShoppingListItem(
		Long userId,
		AddCategoryShoppingListItemRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		CategoryItemTarget target = resolveCategoryTarget(request);

		shoppingList.addCategoryItem(
			target.categoryId(),
			target.categoryName(),
			request.quantityOrDefault()
		);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse scanShoppingListItem(
		Long userId,
		ScanShoppingListItemRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());
		int quantity = request.quantityOrDefault();
		int targetQuantity = calculateScannedTargetQuantity(
			shoppingList,
			productId,
			quantity
		);

		productSummaryReader.validatePurchasable(productId, targetQuantity, storeId);

		shoppingList.addScannedItem(
			productId,
			quantity
		);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse decreaseShoppingListItemQuantityByScan(
		Long userId,
		ScanShoppingListItemRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());

		shoppingList.decreaseQuantityByScan(
			productId,
			request.quantityOrDefault()
		);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemQuantity(
		Long userId,
		Long shoppingListItemId,
		ChangeShoppingListItemQuantityRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(shoppingListItemId);

		int newQuantity = request.quantity();

		if (shoppingListItem.isQuantityDecrease(newQuantity)) {
			throw new ShoppingListException(DECREASE_QUANTITY_REQUIRES_SCAN);
		}

		shoppingListItem.changeQuantity(newQuantity);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemChecked(
		Long userId,
		Long shoppingListItemId,
		ChangeShoppingListItemCheckedRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.changeItemChecked(
			shoppingListItemId,
			request.checked()
		);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public ShoppingListResponse removeShoppingListItem(
		Long userId,
		Long shoppingListItemId,
		long storeId
	) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.removeItem(shoppingListItemId);

		return toResponse(shoppingList, storeId);
	}

	@Transactional
	public void cancelShopping(Long userId, long storeId) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		inventoryStockService.increaseStocks(
			resolvedStoreId,
			shoppingList.getShoppingListItems()
		);

		shoppingList.moveProductItemsToCartForNextShopping();
		shoppingList.cancel();

		shoppingListRepository.delete(shoppingList);
		shoppingListRepository.flush();
	}

	@Transactional
	public void completeShopping(Long userId, long storeId) {
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		long resolvedStoreId = StoreIdSupport.resolve(storeId);

		inventoryStockService.increaseUnscannedStocksByShoppingListItems(
			resolvedStoreId,
			shoppingList.getShoppingListItems()
		);

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

	private void validateCheckedItemsPurchasable(Cart cart, long storeId) {
		cart.getCheckedItems().forEach(cartItem ->
			productSummaryReader.validatePurchasable(
				cartItem.getProductId(),
				cartItem.getQuantity(),
				storeId
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

	private CategoryItemTarget resolveCategoryTarget(
		AddCategoryShoppingListItemRequest request
	) {
		if (request.categoryId() != null) {
			Category category = categoryRepository.findByCategoryIdAndActiveTrue(request.categoryId())
				.orElseThrow(() -> new ShoppingListException(INVALID_SHOPPING_LIST_CATEGORY));

			return new CategoryItemTarget(
				category.getCategoryId(),
				category.getCategoryName()
			);
		}

		String categoryName = normalizeCategoryName(request.categoryName());

		return categoryRepository.findFirstByCategoryNameAndActiveTrue(categoryName)
			.map(category -> new CategoryItemTarget(
				category.getCategoryId(),
				category.getCategoryName()
			))
			.orElseGet(() -> resolveByContainingName(categoryName));
	}

	private CategoryItemTarget resolveByContainingName(String categoryName) {
		List<Category> categories = categoryRepository
			.findByCategoryNameContainingAndActiveTrueOrderByCategoryIdAsc(categoryName);

		if (categories.size() == 1) {
			Category category = categories.get(0);

			return new CategoryItemTarget(
				category.getCategoryId(),
				category.getCategoryName()
			);
		}

		return new CategoryItemTarget(
			null,
			categoryName
		);
	}

	private String normalizeCategoryName(String categoryName) {
		if (categoryName == null || categoryName.isBlank()) {
			throw new ShoppingListException(INVALID_SHOPPING_LIST_CATEGORY);
		}

		return categoryName.trim();
	}

	private ShoppingListResponse toResponse(ShoppingList shoppingList, long storeId) {
		List<Long> productIds = shoppingList.getShoppingListItems().stream()
			.filter(ShoppingListItem::isProductItem)
			.map(ShoppingListItem::getProductId)
			.distinct()
			.toList();

		Map<Long, ProductSummaryResponse> productMap = productSummaryReader
			.getProductSummaryMap(productIds, storeId);

		List<Long> categoryIds = shoppingList.getShoppingListItems().stream()
			.filter(ShoppingListItem::isCategoryItem)
			.map(ShoppingListItem::getCategoryId)
			.distinct()
			.toList();

		Map<Long, Long> categoryGridIdMap = categoryRepository
			.findAllByCategoryIdInAndActiveTrue(categoryIds)
			.stream()
			.collect(Collectors.toMap(
				Category::getCategoryId,
				Category::getGridId
			));

		return ShoppingListResponse.from(
			shoppingList,
			productMap,
			categoryGridIdMap
		);
	}

	private record CategoryItemTarget(
		Long categoryId,
		String categoryName
	) {
	}
}