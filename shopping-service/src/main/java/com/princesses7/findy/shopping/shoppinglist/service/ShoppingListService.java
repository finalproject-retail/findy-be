package com.princesses7.findy.shopping.shoppinglist.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		Cart cart = getCartByUserId(userId);

		validateCheckedItemsPurchasable(cart, resolvedStoreId);

		ShoppingList shoppingList = ShoppingList.create(cart);

		inventoryStockService.decreaseStocksByCartItems(
			resolvedStoreId,
			cart.getCheckedItems()
		);

		ShoppingList savedShoppingList = shoppingListRepository.save(shoppingList);

		shoppingAnalyticsEventService.publishShoppingListItemsFromCart(
			userId,
			savedShoppingList,
			RecommendationSource.DIRECT
		);

		return toResponse(savedShoppingList, resolvedStoreId);
	}

	public ShoppingListResponse getShoppingList(Long userId, long storeId) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse addShoppingListItem(
		Long userId,
		AddShoppingListItemRequest request,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		int quantity = request.quantityOrDefault();

		productSummaryReader.validatePurchasable(
			request.productId(),
			quantity,
			resolvedStoreId
		);

		inventoryStockService.decreaseStock(
			resolvedStoreId,
			request.productId(),
			quantity
		);

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

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse addCategoryShoppingListItem(
		Long userId,
		AddCategoryShoppingListItemRequest request,
		long storeId
	) {
		ShoppingList shoppingList = getOrCreateShoppingListByUserId(userId);
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
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());
		int quantity = request.quantityOrDefault();

		int additionalStockQuantity = calculateAdditionalStockQuantityForScan(
			shoppingList,
			productId,
			quantity
		);

		decreaseStockIfNeeded(
			resolvedStoreId,
			productId,
			additionalStockQuantity
		);

		shoppingList.addScannedItem(
			productId,
			quantity
		);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse decreaseShoppingListItemQuantityByScan(
		Long userId,
		ScanShoppingListItemRequest request,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		Long productId = productBarcodeReader.getProductIdByBarcode(request.barcode());
		int quantity = request.quantityOrDefault();

		shoppingList.decreaseQuantityByScan(
			productId,
			quantity
		);

		inventoryStockService.increaseStock(
			resolvedStoreId,
			productId,
			quantity
		);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemQuantity(
		Long userId,
		Long shoppingListItemId,
		ChangeShoppingListItemQuantityRequest request,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(shoppingListItemId);

		int newQuantity = request.quantity();

		if (newQuantity < shoppingListItem.getQuantity()) {
			throw new ShoppingListException(DECREASE_QUANTITY_REQUIRES_SCAN);
		}

		if (shoppingListItem.isProductItem()) {
			int additionalQuantity = newQuantity - shoppingListItem.getQuantity();

			decreaseStockIfNeeded(
				resolvedStoreId,
				shoppingListItem.getProductId(),
				additionalQuantity
			);
		}

		shoppingListItem.changeQuantity(newQuantity);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse changeShoppingListItemChecked(
		Long userId,
		Long shoppingListItemId,
		ChangeShoppingListItemCheckedRequest request,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);

		shoppingList.changeItemChecked(
			shoppingListItemId,
			request.checked()
		);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse removeShoppingListItem(
		Long userId,
		Long shoppingListItemId,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(shoppingListItemId);

		restoreStockIfProductItem(
			resolvedStoreId,
			shoppingListItem
		);

		shoppingList.removeItem(shoppingListItemId);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public ShoppingListResponse returnShoppingListItemToCart(
		Long userId,
		Long shoppingListItemId,
		long storeId
	) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);
		ShoppingListItem shoppingListItem = shoppingList.getShoppingListItem(shoppingListItemId);

		restoreStockIfProductItem(
			resolvedStoreId,
			shoppingListItem
		);

		shoppingList.returnItemToCart(shoppingListItemId);

		return toResponse(shoppingList, resolvedStoreId);
	}

	@Transactional
	public void cancelShopping(Long userId, long storeId) {
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);

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
		long resolvedStoreId = StoreIdSupport.resolve(storeId);
		ShoppingList shoppingList = getShoppingListByUserId(userId);

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

	private ShoppingList getOrCreateShoppingListByUserId(Long userId) {
		return shoppingListRepository.findByUserId(userId)
			.orElseGet(() -> createEmptyShoppingList(userId));
	}

	private ShoppingList createEmptyShoppingList(Long userId) {
		Cart cart = cartRepository.findByUserId(userId)
			.orElseGet(() -> cartRepository.save(Cart.create(userId)));

		ShoppingList shoppingList = ShoppingList.createEmpty(cart);

		return shoppingListRepository.save(shoppingList);
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

	private int calculateAdditionalStockQuantityForScan(
		ShoppingList shoppingList,
		Long productId,
		int scanQuantity
	) {
		return shoppingList.getShoppingListItems().stream()
			.filter(ShoppingListItem::isProductItem)
			.filter(item -> item.hasSameProduct(productId))
			.findFirst()
			.map(item -> Math.max(
				item.getScannedQuantity() + scanQuantity - item.getQuantity(),
				0
			))
			.orElse(scanQuantity);
	}

	private void decreaseStockIfNeeded(
		long storeId,
		Long productId,
		int quantity
	) {
		if (quantity < 1) {
			return;
		}

		productSummaryReader.validatePurchasable(productId, quantity, storeId);
		inventoryStockService.decreaseStock(storeId, productId, quantity);
	}

	private void restoreStockIfProductItem(
		long storeId,
		ShoppingListItem shoppingListItem
	) {
		if (!shoppingListItem.isProductItem()) {
			return;
		}

		inventoryStockService.increaseStock(
			storeId,
			shoppingListItem.getProductId(),
			shoppingListItem.getQuantity()
		);
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
			.filter(Objects::nonNull)
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