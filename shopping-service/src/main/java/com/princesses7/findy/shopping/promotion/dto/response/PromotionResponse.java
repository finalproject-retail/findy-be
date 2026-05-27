package com.princesses7.findy.shopping.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record PromotionResponse(
	Long promotionId,
	String promotionName,
	PromotionType promotionType,
	String benefitText,
	Integer minPurchaseAmount,
	Integer buyQuantity,
	Integer getQuantity,
	String giftItem,
	BigDecimal discountRate,
	LocalDateTime startAt,
	LocalDateTime endAt,
	PromotionStatus status
) {

	public static PromotionResponse from(Promotion promotion) {
		LocalDateTime now = LocalDateTime.now();

		return new PromotionResponse(
			promotion.getPromotionId(),
			promotion.getPromotionName(),
			promotion.getPromotionType(),
			promotion.getBenefitText(),
			promotion.getMinPurchaseAmount(),
			promotion.getBuyQuantity(),
			promotion.getGetQuantity(),
			promotion.getGiftItem(),
			promotion.getDiscountRate(),
			promotion.getStartAt(),
			promotion.getEndAt(),
			promotion.calculateStatus(now)
		);
	}
}