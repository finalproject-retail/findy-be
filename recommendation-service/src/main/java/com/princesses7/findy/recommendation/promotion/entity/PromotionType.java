package com.princesses7.findy.recommendation.promotion.entity;

public enum PromotionType {

	DISCOUNT,
	BOGO,
	ONE_PLUS_ONE,
	BUNDLE,
	GIFT;

	public boolean isBogoLike() {
		return this == BOGO
			|| this == ONE_PLUS_ONE
			|| this == BUNDLE;
	}
}
