package com.princesses7.findy.shopping.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record ApplicablePromotionResponse(
	Long promotionId,
	Long promotionProductId,
	Long productId,
	String promotionName,
	PromotionType promotionType,
	String benefitText,
	Integer promotionPrice,
	BigDecimal discountRate,
	Integer minPurchaseAmount,
	Integer buyQuantity,
	Integer getQuantity,
	String giftItem,
	LocalDateTime startAt,
	LocalDateTime endAt,
	PromotionStatus status
) {

	public static ApplicablePromotionResponse from(PromotionProduct promotionProduct) {
		Promotion promotion = promotionProduct.getPromotion();
		LocalDateTime now = LocalDateTime.now();

		return new ApplicablePromotionResponse(
			promotion.getPromotionId(),
			promotionProduct.getPromotionProductId(),
			promotionProduct.getProductId(),
			promotion.getPromotionName(),
			promotion.getPromotionType(),
			promotion.getBenefitText(),
			promotionProduct.getPromotionPrice(),
			promotion.getDiscountRate(),
			promotion.getMinPurchaseAmount(),
			promotion.getBuyQuantity(),
			promotion.getGetQuantity(),
			promotion.getGiftItem(),
			promotion.getStartAt(),
			promotion.getEndAt(),
			promotion.calculateStatus(now)
		);
	}
}