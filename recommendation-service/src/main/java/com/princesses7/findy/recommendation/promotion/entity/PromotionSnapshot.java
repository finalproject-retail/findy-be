package com.princesses7.findy.recommendation.promotion.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "promotions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionSnapshot {

	@Id
	@Column(name = "promotion_id")
	private Long promotionId;

	@Column(name = "promotion_name", nullable = false)
	private String promotionName;

	@Enumerated(EnumType.STRING)
	@Column(name = "promotion_type", nullable = false)
	private PromotionType promotionType;

	@Column(name = "min_purchase_amount")
	private Integer minPurchaseAmount;

	@Column(name = "buy_quantity")
	private Integer buyQuantity;

	@Column(name = "get_quantity")
	private Integer getQuantity;

	@Column(name = "gift_item")
	private String giftItem;

	@Column(name = "discount_rate", precision = 5, scale = 2)
	private BigDecimal discountRate;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PromotionStatus status;

	public boolean isActive(LocalDateTime now) {
		return status != PromotionStatus.ENDED
			&& !now.isBefore(startAt)
			&& !now.isAfter(endAt);
	}

	public String getBenefitText() {
		if (promotionType == PromotionType.DISCOUNT) {
			return createDiscountText();
		}

		if (promotionType == PromotionType.BOGO) {
			return createBogoText();
		}

		if (promotionType == PromotionType.GIFT) {
			return createGiftText();
		}

		return "행사 혜택";
	}

	private String createDiscountText() {
		if (discountRate == null) {
			return "할인 행사";
		}

		return discountRate.stripTrailingZeros().toPlainString() + "% 할인";
	}

	private String createBogoText() {
		if (buyQuantity == null || buyQuantity < 1 || getQuantity == null || getQuantity < 1) {
			return "묶음 행사";
		}

		return buyQuantity + "+" + getQuantity + " 행사";
	}

	private String createGiftText() {
		if (giftItem == null || giftItem.isBlank()) {
			return "사은품 증정";
		}

		return giftItem + " 증정";
	}
}