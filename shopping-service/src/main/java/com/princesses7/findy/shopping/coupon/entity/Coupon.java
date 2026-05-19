package com.princesses7.findy.shopping.coupon.entity;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.exception.CouponException;
import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

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

@Entity
@Getter
@Table(name = "coupons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "coupon_id")
	private Long couponId;

	@Column(name = "coupon_name", nullable = false)
	private String couponName;

	@Enumerated(EnumType.STRING)
	@Column(name = "coupon_type", nullable = false)
	private CouponType couponType;

	@Enumerated(EnumType.STRING)
	@Column(name = "discount_type", nullable = false)
	private DiscountType discountType;

	@Column(name = "discount_value", nullable = false)
	private Integer discountValue;

	@Column(name = "is_stackable", nullable = false)
	private boolean stackable;

	@Column(name = "min_order_amount", nullable = false)
	private Integer minOrderAmount;

	@Column(name = "start_at", nullable = false)
	private LocalDateTime startAt;

	@Column(name = "end_at", nullable = false)
	private LocalDateTime endAt;

	@Enumerated(EnumType.STRING)
	@Column(name = "period_type", nullable = false)
	private PeriodType periodType;

	@Column(name = "days_limit")
	private Integer daysLimit;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	private Coupon(
		String couponName,
		CouponType couponType,
		DiscountType discountType,
		Integer discountValue,
		boolean stackable,
		Integer minOrderAmount,
		LocalDateTime startAt,
		LocalDateTime endAt,
		PeriodType periodType,
		Integer daysLimit
	) {
		validatePeriod(startAt, endAt, periodType, daysLimit);
		validateDiscount(discountType, discountValue);

		this.couponName = couponName;
		this.couponType = couponType;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.stackable = stackable;
		this.minOrderAmount = minOrderAmount == null ? 0 : minOrderAmount;
		this.startAt = startAt;
		this.endAt = endAt;
		this.periodType = periodType;
		this.daysLimit = daysLimit;
		this.active = true;
	}

	public static Coupon create(
		String couponName,
		CouponType couponType,
		DiscountType discountType,
		Integer discountValue,
		boolean stackable,
		Integer minOrderAmount,
		LocalDateTime startAt,
		LocalDateTime endAt,
		PeriodType periodType,
		Integer daysLimit
	) {
		return new Coupon(
			couponName,
			couponType,
			discountType,
			discountValue,
			stackable,
			minOrderAmount,
			startAt,
			endAt,
			periodType,
			daysLimit
		);
	}

	public void update(
		String couponName,
		CouponType couponType,
		DiscountType discountType,
		Integer discountValue,
		boolean stackable,
		Integer minOrderAmount,
		LocalDateTime startAt,
		LocalDateTime endAt,
		PeriodType periodType,
		Integer daysLimit
	) {
		validatePeriod(startAt, endAt, periodType, daysLimit);
		validateDiscount(discountType, discountValue);

		this.couponName = couponName;
		this.couponType = couponType;
		this.discountType = discountType;
		this.discountValue = discountValue;
		this.stackable = stackable;
		this.minOrderAmount = minOrderAmount == null ? 0 : minOrderAmount;
		this.startAt = startAt;
		this.endAt = endAt;
		this.periodType = periodType;
		this.daysLimit = daysLimit;
	}

	public void deactivate() {
		if (!this.active) {
			throw new CouponException(COUPON_ALREADY_INACTIVE);
		}

		this.active = false;
	}

	public boolean isAvailable(LocalDateTime now) {
		return now != null
			&& active
			&& !now.isBefore(startAt)
			&& !now.isAfter(endAt);
	}

	private static void validatePeriod(
		LocalDateTime startAt,
		LocalDateTime endAt,
		PeriodType periodType,
		Integer daysLimit
	) {
		if (startAt == null || endAt == null || periodType == null) {
			throw new CouponException(INVALID_COUPON_PERIOD);
		}

		if (endAt.isBefore(startAt)) {
			throw new CouponException(INVALID_COUPON_PERIOD);
		}

		if (periodType == PeriodType.RELATIVE && (daysLimit == null || daysLimit < 1)) {
			throw new CouponException(INVALID_COUPON_PERIOD);
		}
	}

	private static void validateDiscount(
		DiscountType discountType,
		Integer discountValue
	) {
		if (discountType == null || discountValue == null || discountValue < 1) {
			throw new CouponException(INVALID_COUPON_DISCOUNT_VALUE);
		}

		if (discountType == DiscountType.RATE && discountValue > 100) {
			throw new CouponException(INVALID_COUPON_DISCOUNT_VALUE);
		}
	}
}