package com.princesses7.findy.shopping.coupon.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.coupon.dto.request.CreateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.response.CouponResponse;
import com.princesses7.findy.shopping.coupon.entity.Coupon;
import com.princesses7.findy.shopping.coupon.repository.CouponRepository;
import com.princesses7.findy.shopping.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

	private final CouponRepository couponRepository;

	@Transactional
	public CouponResponse createCoupon(CreateCouponRequest request) {
		Coupon coupon = Coupon.create(
			request.couponName(),
			request.couponType(),
			request.discountType(),
			request.discountValue(),
			request.stackableOrDefault(),
			request.minOrderAmountOrDefault(),
			request.startAt(),
			request.endAt(),
			request.periodType(),
			request.daysLimit()
		);

		Coupon savedCoupon = couponRepository.save(coupon);

		return CouponResponse.from(savedCoupon);
	}

	private Coupon getCoupon(Long couponId) {
		return couponRepository.findByCouponId(couponId)
			.orElseThrow(() -> new BaseException(COUPON_NOT_FOUND));
	}
}