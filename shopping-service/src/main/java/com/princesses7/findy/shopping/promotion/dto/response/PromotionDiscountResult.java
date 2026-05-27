package com.princesses7.findy.shopping.promotion.dto.response;

import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record PromotionDiscountResult(
	Long promotionId,
	String promotionName,
	PromotionType promotionType,
	String benefitText,
	int discountAmount
) {

	public static PromotionDiscountResult none() {
		return new PromotionDiscountResult(
			null,
			null,
			null,
			null,
			0
		);
	}
}