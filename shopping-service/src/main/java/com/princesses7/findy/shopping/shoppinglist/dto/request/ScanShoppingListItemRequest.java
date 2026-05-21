package com.princesses7.findy.shopping.shoppinglist.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ScanShoppingListItemRequest(

	@NotBlank(message = "바코드는 필수입니다.")
	String barcode,

	@Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
	Integer quantity
) {

	public int quantityOrDefault() {
		return quantity == null ? 1 : quantity;
	}
}