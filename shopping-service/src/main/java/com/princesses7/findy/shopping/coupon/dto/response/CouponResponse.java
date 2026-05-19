package com.princesses7.findy.shopping.coupon.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.entity.Coupon;
import com.princesses7.findy.shopping.coupon.entity.CouponType;
import com.princesses7.findy.shopping.coupon.entity.DiscountType;
import com.princesses7.findy.shopping.coupon.entity.PeriodType;

public record CouponResponse(
	Long couponId,
	String couponName,
	CouponType couponType,
	DiscountType discountType,
	Integer discountValue,
	boolean isStackable,
	Integer minOrderAmount,
	LocalDateTime startAt,
	LocalDateTime endAt,
	PeriodType periodType,
	Integer daysLimit,
	boolean isActive
) {

	public static CouponResponse from(Coupon coupon) {
		return new CouponResponse(
			coupon.getCouponId(),
			coupon.getCouponName(),
			coupon.getCouponType(),
			coupon.getDiscountType(),
			coupon.getDiscountValue(),
			coupon.isStackable(),
			coupon.getMinOrderAmount(),
			coupon.getStartAt(),
			coupon.getEndAt(),
			coupon.getPeriodType(),
			coupon.getDaysLimit(),
			coupon.isActive()
		);
	}
}