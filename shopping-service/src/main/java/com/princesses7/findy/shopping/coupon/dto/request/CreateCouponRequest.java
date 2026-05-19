package com.princesses7.findy.shopping.coupon.dto.request;

import java.time.LocalDateTime;

import com.princesses7.findy.shopping.coupon.entity.CouponType;
import com.princesses7.findy.shopping.coupon.entity.DiscountType;
import com.princesses7.findy.shopping.coupon.entity.PeriodType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCouponRequest(

	@NotBlank(message = "쿠폰명은 필수입니다.")
	String couponName,

	@NotNull(message = "쿠폰 유형은 필수입니다.")
	CouponType couponType,

	@NotNull(message = "할인 유형은 필수입니다.")
	DiscountType discountType,

	@NotNull(message = "할인 값은 필수입니다.")
	@Min(value = 1, message = "할인 값은 1 이상이어야 합니다.")
	Integer discountValue,

	Boolean isStackable,

	@Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
	Integer minOrderAmount,

	@NotNull(message = "시작일시는 필수입니다.")
	LocalDateTime startAt,

	@NotNull(message = "종료일시는 필수입니다.")
	LocalDateTime endAt,

	@NotNull(message = "기간 유형은 필수입니다.")
	PeriodType periodType,

	@Min(value = 1, message = "유효 일수는 1일 이상이어야 합니다.")
	Integer daysLimit
) {

	public boolean stackableOrDefault() {
		return isStackable != null && isStackable;
	}

	public int minOrderAmountOrDefault() {
		return minOrderAmount == null ? 0 : minOrderAmount;
	}
}