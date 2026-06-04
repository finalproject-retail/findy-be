package com.princesses7.findy.shopping.shoppinglist.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;

public record ShoppingListResponse(
	Long shoppingListId,
	Long cartId,
	int totalItemCount,
	long scannedItemCount,
	int totalAmount,
	int scannedAmount,
	List<ShoppingListItemResponse> items,
	List<Long> destinationGridIds
) {

	public static ShoppingListResponse from(
		ShoppingList shoppingList,
		Map<Long, ProductSummaryResponse> productMap
	) {
		List<ShoppingListItemResponse> items = shoppingList.getShoppingListItems()
			.stream()
			.map(item -> ShoppingListItemResponse.from(
				item,
				getProductSummary(item, productMap)
			))
			.toList();

		int totalAmount = items.stream()
			.mapToInt(ShoppingListItemResponse::itemTotalAmount)
			.sum();

		int scannedAmount = items.stream()
			.mapToInt(ShoppingListItemResponse::scannedAmount)
			.sum();

		return new ShoppingListResponse(
			shoppingList.getShoppingListId(),
			shoppingList.getCart().getCartId(),
			shoppingList.getTotalItemCount(),
			shoppingList.getScannedItemCount(),
			totalAmount,
			scannedAmount,
			items,
			extractDestinationGridIds(items)
		);
	}

	private static ProductSummaryResponse getProductSummary(
		ShoppingListItem item,
		Map<Long, ProductSummaryResponse> productMap
	) {
		if (item.isCategoryItem()) {
			return null;
		}

		return productMap.get(item.getProductId());
	}

	/**
	 * map-service 경로 API({@code destinationGridIds})에 바로 넣을 수 있는 목록.
	 * 쇼핑 리스트 순서를 유지하고, 연속 중복 격자·gridId 없음 항목은 제외한다.
	 */
	private static List<Long> extractDestinationGridIds(List<ShoppingListItemResponse> items) {
		List<Long> destinationGridIds = new ArrayList<>();
		Long previousGridId = null;

		for (ShoppingListItemResponse item : items) {
			if (item.product() == null || item.product().gridId() == null) {
				continue;
			}

			Long gridId = item.product().gridId();
			if (gridId.equals(previousGridId)) {
				continue;
			}

			destinationGridIds.add(gridId);
			previousGridId = gridId;
		}

		return destinationGridIds;
	}
}