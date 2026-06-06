package com.princesses7.findy.recommendation.coupon.entity;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponSnapshot extends BaseTimeEntity {

	private static final String PERCENT = "PERCENT";
	private static final String AMOUNT = "AMOUNT";

	@Id
	@Column(name = "coupon_id")
	private Long couponId;

	@Column(name = "coupon_name", nullable = false)
	private String couponName;

	@Column(name = "coupon_type", nullable = false)
	private String couponType;

	@Column(name = "discount_type", nullable = false)
	private String discountType;

	@Column(name = "discount_value", nullable = false)
	private Integer discountValue;

	@Column(name = "min_order_amount", nullable = false)
	private Integer minOrderAmount;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	public boolean isAvailable(LocalDateTime now) {
		return active
			&& !now.isBefore(startAt)
			&& !now.isAfter(endAt);
	}

	public String getBenefitText() {
		if (PERCENT.equals(discountType)) {
			return discountValue + "% 할인";
		}

		if (AMOUNT.equals(discountType)) {
			return discountValue + "원 할인";
		}

		return "쿠폰 혜택";
	}
}