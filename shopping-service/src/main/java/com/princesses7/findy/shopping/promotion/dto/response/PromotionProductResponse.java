package com.princesses7.findy.shopping.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record PromotionProductResponse(
	Long promotionProductId,
	Long promotionId,
	String promotionName,
	PromotionType promotionType,
	String benefitText,
	Long productId,
	Integer promotionPrice,
	Long gridId,
	BigDecimal discountRate,
	Integer minPurchaseAmount,
	Integer buyQuantity,
	Integer getQuantity,
	String giftItem,
	LocalDateTime startAt,
	LocalDateTime endAt,
	PromotionStatus status
) {

	public static PromotionProductResponse from(PromotionProduct promotionProduct) {
		Promotion promotion = promotionProduct.getPromotion();
		LocalDateTime now = LocalDateTime.now();

		return new PromotionProductResponse(
			promotionProduct.getPromotionProductId(),
			promotion.getPromotionId(),
			promotion.getPromotionName(),
			promotion.getPromotionType(),
			promotion.getBenefitText(),
			promotionProduct.getProductId(),
			promotionProduct.getPromotionPrice(),
			promotionProduct.getGridId(),
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