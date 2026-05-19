package com.princesses7.findy.shopping.coupon.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.entity.Coupon;
import com.princesses7.findy.shopping.coupon.entity.CouponType;
import com.princesses7.findy.shopping.coupon.entity.DiscountType;
import com.princesses7.findy.shopping.coupon.entity.PeriodType;
import com.princesses7.findy.shopping.coupon.entity.UserCoupon;

public record UserCouponResponse(
	Long userCouponId,
	Long couponId,
	String couponName,
	CouponType couponType,
	DiscountType discountType,
	Integer discountValue,
	boolean isStackable,
	Integer minOrderAmount,
	PeriodType periodType,
	Integer daysLimit,
	boolean isUsed,
	boolean isExpired,
	LocalDateTime downloadedAt,
	LocalDateTime expiresAt,
	LocalDateTime usedAt
) {

	public static UserCouponResponse from(UserCoupon userCoupon, LocalDateTime now) {
		Coupon coupon = userCoupon.getCoupon();

		return new UserCouponResponse(
			userCoupon.getUserCouponId(),
			coupon.getCouponId(),
			coupon.getCouponName(),
			coupon.getCouponType(),
			coupon.getDiscountType(),
			coupon.getDiscountValue(),
			coupon.isStackable(),
			coupon.getMinOrderAmount(),
			coupon.getPeriodType(),
			coupon.getDaysLimit(),
			userCoupon.isUsed(),
			userCoupon.isExpired(now),
			userCoupon.getDownloadedAt(),
			userCoupon.getExpiresAt(),
			userCoupon.getUsedAt()
		);
	}
}