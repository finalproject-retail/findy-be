package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.princesses7.findy.shopping.product.dto.response.ProductSummaryResponse;
import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;
import com.princesses7.findy.shopping.shoppinglist.type.ScanStatus;
import com.princesses7.findy.shopping.shoppinglist.type.ShoppingListItemType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShoppingListItemResponse(
	Long shoppingListItemId,
	ShoppingListItemType itemType,
	ProductSummaryResponse product,
	CategoryShoppingListItemResponse category,
	int quantity,
	Integer scannedQuantity,
	boolean checked,
	ScanStatus scanStatus,
	Integer itemTotalAmount,
	Integer scannedAmount
) {

	public static ShoppingListItemResponse from(
		ShoppingListItem item,
		ProductSummaryResponse product,
		Long categoryGridId
	) {
		if (item.isCategoryItem()) {
			return fromCategory(item, categoryGridId);
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
			product,
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
		ShoppingListItem item,
		Long categoryGridId
	) {
		CategoryShoppingListItemResponse category = new CategoryShoppingListItemResponse(
			item.getCategoryId(),
			item.getCategoryName(),
			categoryGridId
		);

		return new ShoppingListItemResponse(
			item.getShoppingListItemId(),
			item.getItemType(),
			null,
			category,
			item.getQuantity(),
			null,
			item.isChecked(),
			item.getScanStatus(),
			null,
			null
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
