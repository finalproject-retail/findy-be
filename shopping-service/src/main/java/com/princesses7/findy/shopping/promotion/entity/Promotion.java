package com.princesses7.findy.shopping.promotion.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.promotion.exception.PromotionException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "promotions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Promotion extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	private Promotion(
		String promotionName,
		PromotionType promotionType,
		Integer minPurchaseAmount,
		Integer buyQuantity,
		Integer getQuantity,
		String giftItem,
		BigDecimal discountRate,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		validatePromotionName(promotionName);
		validatePeriod(startAt, endAt);
		validateBenefit(promotionType, minPurchaseAmount, buyQuantity, getQuantity, giftItem, discountRate);

		this.promotionName = promotionName;
		this.promotionType = promotionType;
		this.minPurchaseAmount = minPurchaseAmount;
		this.buyQuantity = buyQuantity;
		this.getQuantity = getQuantity;
		this.giftItem = giftItem;
		this.discountRate = discountRate;
		this.startAt = startAt;
		this.endAt = endAt;
		this.status = calculateStatusByPeriod(startAt, endAt, LocalDateTime.now());
	}

	public static Promotion create(
		String promotionName,
		PromotionType promotionType,
		Integer minPurchaseAmount,
		Integer buyQuantity,
		Integer getQuantity,
		String giftItem,
		BigDecimal discountRate,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		return new Promotion(
			promotionName,
			promotionType,
			minPurchaseAmount,
			buyQuantity,
			getQuantity,
			giftItem,
			discountRate,
			startAt,
			endAt
		);
	}

	public void update(
		String promotionName,
		PromotionType promotionType,
		Integer minPurchaseAmount,
		Integer buyQuantity,
		Integer getQuantity,
		String giftItem,
		BigDecimal discountRate,
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		validatePromotionName(promotionName);
		validatePeriod(startAt, endAt);
		validateBenefit(promotionType, minPurchaseAmount, buyQuantity, getQuantity, giftItem, discountRate);

		this.promotionName = promotionName;
		this.promotionType = promotionType;
		this.minPurchaseAmount = minPurchaseAmount;
		this.buyQuantity = buyQuantity;
		this.getQuantity = getQuantity;
		this.giftItem = giftItem;
		this.discountRate = discountRate;
		this.startAt = startAt;
		this.endAt = endAt;
		this.status = calculateStatus(LocalDateTime.now());
	}

	public void end() {
		if (status == PromotionStatus.ENDED) {
			throw new PromotionException(PROMOTION_ALREADY_ENDED);
		}

		this.status = PromotionStatus.ENDED;
	}

	public PromotionStatus calculateStatus(LocalDateTime now) {
		if (status == PromotionStatus.ENDED) {
			return PromotionStatus.ENDED;
		}

		return calculateStatusByPeriod(startAt, endAt, now);
	}

	public boolean isActive(LocalDateTime now) {
		return calculateStatus(now) == PromotionStatus.ACTIVE;
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

	private static PromotionStatus calculateStatusByPeriod(
		LocalDateTime startAt,
		LocalDateTime endAt,
		LocalDateTime now
	) {
		if (now.isBefore(startAt)) {
			return PromotionStatus.SCHEDULED;
		}

		if (now.isAfter(endAt)) {
			return PromotionStatus.ENDED;
		}

		return PromotionStatus.ACTIVE;
	}

	private static void validatePromotionName(String promotionName) {
		if (promotionName == null || promotionName.isBlank()) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}
	}

	private static void validatePeriod(
		LocalDateTime startAt,
		LocalDateTime endAt
	) {
		if (startAt == null || endAt == null || endAt.isBefore(startAt)) {
			throw new PromotionException(INVALID_PROMOTION_PERIOD);
		}
	}

	private static void validateBenefit(
		PromotionType promotionType,
		Integer minPurchaseAmount,
		Integer buyQuantity,
		Integer getQuantity,
		String giftItem,
		BigDecimal discountRate
	) {
		if (promotionType == null) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}

		if (discountRate != null && isInvalidDiscountRate(discountRate)) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}

		if (promotionType == PromotionType.BOGO) {
			validateBogoBenefit(buyQuantity, getQuantity);
		}

		if (promotionType == PromotionType.GIFT) {
			validateGiftBenefit(minPurchaseAmount, giftItem);
		}
	}

	private static void validateBogoBenefit(
		Integer buyQuantity,
		Integer getQuantity
	) {
		if (buyQuantity == null || buyQuantity < 1) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}

		if (getQuantity == null || getQuantity < 1) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}
	}

	private static void validateGiftBenefit(
		Integer minPurchaseAmount,
		String giftItem
	) {
		if (minPurchaseAmount == null || minPurchaseAmount < 0) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}

		if (giftItem == null || giftItem.isBlank()) {
			throw new PromotionException(INVALID_PROMOTION_BENEFIT);
		}
	}

	private static boolean isInvalidDiscountRate(BigDecimal discountRate) {
		return discountRate.compareTo(BigDecimal.ZERO) < 0
			|| discountRate.compareTo(BigDecimal.valueOf(100)) > 0;
	}
}