package com.princesses7.findy.user.preference.dto.response;

import com.princesses7.findy.user.preference.entity.ShoppingStyle;

public record ShoppingStyleResponse(
	Long shoppingStyleId,
	String styleName
) {

	public static ShoppingStyleResponse from(ShoppingStyle shoppingStyle) {
		return new ShoppingStyleResponse(
			shoppingStyle.getShoppingStyleId(),
			shoppingStyle.getStyleName()
		);
	}
}