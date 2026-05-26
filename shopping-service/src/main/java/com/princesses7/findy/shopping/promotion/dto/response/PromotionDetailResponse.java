package com.princesses7.findy.shopping.promotion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionType;

public record PromotionDetailResponse(
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
	PromotionStatus status,
	List<PromotionProductResponse> promotionProducts
) {

	public static PromotionDetailResponse of(
		Promotion promotion,
		List<PromotionProduct> promotionProducts
	) {
		LocalDateTime now = LocalDateTime.now();

		return new PromotionDetailResponse(
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
			promotion.calculateStatus(now),
			promotionProducts.stream()
				.map(PromotionProductResponse::from)
				.toList()
		);
	}
}