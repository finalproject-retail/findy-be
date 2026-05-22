package com.princesses7.findy.shopping.coupon.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.entity.Coupon;
import com.princesses7.findy.shopping.coupon.entity.UserCoupon;

public record AvailableOrderCouponResponse(
	Long userCouponId,
	Long couponId,
	String couponName,
	String couponType,
	String discountType,
	int discountValue,
	int minOrderAmount,
	LocalDateTime expiresAt,
	int expectedDiscountAmount
) {

	public static AvailableOrderCouponResponse of(
		UserCoupon userCoupon,
		int orderAmount
	) {
		Coupon coupon = userCoupon.getCoupon();

		return new AvailableOrderCouponResponse(
			userCoupon.getUserCouponId(),
			coupon.getCouponId(),
			coupon.getCouponName(),
			coupon.getCouponType().name(),
			coupon.getDiscountType().name(),
			coupon.getDiscountValue(),
			coupon.getMinOrderAmount(),
			userCoupon.getExpiresAt(),
			calculateExpectedDiscountAmount(coupon, orderAmount)
		);
	}

	private static int calculateExpectedDiscountAmount(
		Coupon coupon,
		int orderAmount
	) {
		if (orderAmount < coupon.getMinOrderAmount()) {
			return 0;
		}

		if (coupon.getDiscountType().name().equals("RATE")) {
			return orderAmount * coupon.getDiscountValue() / 100;
		}

		return Math.min(coupon.getDiscountValue(), orderAmount);
	}
}