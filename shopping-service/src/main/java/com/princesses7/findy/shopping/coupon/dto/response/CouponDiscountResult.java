package com.princesses7.findy.shopping.coupon.dto.response;

public record CouponDiscountResult(
	Long couponId,
	int discountAmount
) {

	public static CouponDiscountResult none() {
		return new CouponDiscountResult(null, 0);
	}
}