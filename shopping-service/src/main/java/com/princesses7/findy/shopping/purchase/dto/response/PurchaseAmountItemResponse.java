package com.princesses7.findy.shopping.purchase.dto.response;

import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record PurchaseAmountItemResponse(
	Long productId,
	int quantity,
	int productPrice,
	int totalAmount,
	int discountAmount,
	int finalAmount,
	Long appliedPromotionId,
	String appliedPromotionName,
	PromotionType appliedPromotionType,
	String promotionBenefitText
) {
}