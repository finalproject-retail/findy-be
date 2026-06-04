package com.princesses7.findy.shopping.shoppinglist.dto.request;

import jakarta.validation.constraints.Min;

public record AddCategoryShoppingListItemRequest(
	Long categoryId,
	String categoryName,

	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	Integer quantity
) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}
}