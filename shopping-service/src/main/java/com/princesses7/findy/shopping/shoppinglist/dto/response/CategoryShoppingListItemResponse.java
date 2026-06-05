package com.princesses7.findy.shopping.shoppinglist.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryShoppingListItemResponse(
	Long categoryId,
	String categoryName,
	Long gridId
) {
}