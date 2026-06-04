package com.princesses7.findy.shopping.shoppinglist.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeShoppingListItemCheckedRequest(
	@NotNull(message = "체크 여부는 필수입니다.")
	Boolean checked
) {
}