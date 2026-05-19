package com.princesses7.findy.shopping.coupon.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.coupon.dto.request.CreateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.request.UpdateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.response.CouponPageResponse;
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

	public CouponPageResponse getAdminCoupons(
		String keyword,
		Boolean active,
		int page,
		int size
	) {
		validatePageRequest(page, size);

		Pageable pageable = PageRequest.of(page, size);

		Page<CouponResponse> coupons = couponRepository.searchAdminCoupons(
			normalizeKeyword(keyword),
			active,
			pageable
		).map(CouponResponse::from);

		return CouponPageResponse.from(coupons);
	}

	public CouponResponse getAdminCoupon(Long couponId) {
		Coupon coupon = getCoupon(couponId);

		return CouponResponse.from(coupon);
	}

	private void validatePageRequest(int page, int size) {
		if (page < 0 || size < 1 || size > 100) {
			throw new BaseException(INVALID_INPUT_VALUE);
		}
	}

	private String normalizeKeyword(String keyword) {
		if (keyword == null || keyword.isBlank()) {
			return null;
		}

		return keyword.trim();
	}

	@Transactional
	public CouponResponse updateCoupon(Long couponId, UpdateCouponRequest request) {
		Coupon coupon = getCoupon(couponId);

		coupon.update(
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

		return CouponResponse.from(coupon);
	}

	@Transactional
	public void deactivateCoupon(Long couponId) {
		Coupon coupon = getCoupon(couponId);

		coupon.deactivate();
	}
}