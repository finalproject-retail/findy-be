package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;
import com.princesses7.findy.shopping.shoppinglist.type.ShoppingListItemType;

public record ShoppingListItemResponse(
	Long shoppingListItemId,
	ShoppingListItemType itemType,
	Long productId,
	ProductSummaryResponse product,
	Long categoryId,
	String categoryName,
	CategoryShoppingListItemResponse category,
	int quantity,
	int scannedQuantity,
	boolean checked,
	ScanStatus scanStatus,
	int itemTotalAmount,
	int scannedAmount
) {

	public static ShoppingListItemResponse from(
		ShoppingListItem item,
		ProductSummaryResponse product
	) {
		if (item.isCategoryItem()) {
			return fromCategory(item);
		}

		return fromProduct(item, product);
	}

	private static ShoppingListItemResponse fromProduct(
		ShoppingListItem item,
		ProductSummaryResponse product
	) {
		return new ShoppingListItemResponse(
			item.getShoppingListItemId(),
			item.getItemType(),
			item.getProductId(),
			product,
			null,
			null,
			null,
			item.getQuantity(),
			item.getScannedQuantity(),
			item.isChecked(),
			item.getScanStatus(),
			calculateAmount(product, item.getQuantity()),
			calculateAmount(product, item.getScannedQuantity())
		);
	}

	private static ShoppingListItemResponse fromCategory(
		ShoppingListItem item
	) {
		CategoryShoppingListItemResponse category = new CategoryShoppingListItemResponse(
			item.getCategoryId(),
			item.getCategoryName()
		);

		return new ShoppingListItemResponse(
			item.getShoppingListItemId(),
			item.getItemType(),
			null,
			null,
			item.getCategoryId(),
			item.getCategoryName(),
			category,
			item.getQuantity(),
			item.getCompletedQuantity(),
			item.isChecked(),
			item.getScanStatus(),
			0,
			0
		);
	}

	private static int calculateAmount(
		ProductSummaryResponse product,
		int quantity
	) {
		if (product == null) {
			return 0;
		}

		return product.calculateAmount(quantity);
	}
}