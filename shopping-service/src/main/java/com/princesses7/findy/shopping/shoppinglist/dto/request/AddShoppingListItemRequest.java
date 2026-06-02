package com.princesses7.findy.shopping.shoppinglist.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddShoppingListItemRequest(

	@NotNull(message = "상품 ID는 필수입니다.")
	Long productId,

	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	Integer quantity,

	String recommendationSource,

	Long originalProductId
) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}
}