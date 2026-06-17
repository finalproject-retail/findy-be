package com.princesses7.findy.shopping.promotion.entity;

public enum PromotionType {

	DISCOUNT,
	BOGO,
	ONE_PLUS_ONE,
	TWO_PLUS_ONE,
	BUNDLE,
	GIFT;

	public boolean isBogoLike() {
		return this == BOGO
			|| this == ONE_PLUS_ONE
			|| this == TWO_PLUS_ONE
			|| this == BUNDLE;
	}
}
