package com.princesses7.findy.shopping.coupon.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.coupon.dto.request.CreateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.request.UpdateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.response.CouponDiscountResult;
import com.princesses7.findy.shopping.coupon.dto.response.CouponPageResponse;
import com.princesses7.findy.shopping.coupon.dto.response.CouponResponse;
import com.princesses7.findy.shopping.coupon.dto.response.UserCouponPageResponse;
import com.princesses7.findy.shopping.coupon.dto.response.UserCouponResponse;
import com.princesses7.findy.shopping.coupon.entity.Coupon;
import com.princesses7.findy.shopping.coupon.entity.DiscountType;
import com.princesses7.findy.shopping.coupon.entity.UserCoupon;
import com.princesses7.findy.shopping.coupon.exception.CouponException;
import com.princesses7.findy.shopping.coupon.repository.CouponRepository;
import com.princesses7.findy.shopping.coupon.repository.UserCouponRepository;
import com.princesses7.findy.shopping.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {

	private final CouponRepository couponRepository;
	private final UserCouponRepository userCouponRepository;

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
		String normalizedKeyword = normalizeKeyword(keyword);

		Page<Coupon> coupons = findAdminCoupons(
			normalizedKeyword,
			active,
			pageable
		);

		Page<CouponResponse> responsePage = coupons.map(CouponResponse::from);

		return CouponPageResponse.from(responsePage);
	}

	private Page<Coupon> findAdminCoupons(
		String keyword,
		Boolean active,
		Pageable pageable
	) {
		boolean hasKeyword = keyword != null;

		if (hasKeyword && active != null) {
			return couponRepository.findByCouponNameContainingIgnoreCaseAndActive(
				keyword,
				active,
				pageable
			);
		}

		if (hasKeyword) {
			return couponRepository.findByCouponNameContainingIgnoreCase(
				keyword,
				pageable
			);
		}

		if (active != null) {
			return couponRepository.findByActive(active, pageable);
		}

		return couponRepository.findAll(pageable);
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

	public CouponPageResponse getAvailableCoupons(int page, int size) {
		validatePageRequest(page, size);

		Pageable pageable = PageRequest.of(page, size);
		LocalDateTime now = LocalDateTime.now();

		Page<CouponResponse> coupons = couponRepository.findAvailableCoupons(
			now,
			pageable
		).map(CouponResponse::from);

		return CouponPageResponse.from(coupons);
	}

	public CouponResponse getAvailableCoupon(Long couponId) {
		Coupon coupon = couponRepository.findByCouponIdAndActiveTrue(couponId)
			.orElseThrow(() -> new BaseException(COUPON_NOT_FOUND));

		if (!coupon.isAvailable(LocalDateTime.now())) {
			throw new BaseException(COUPON_NOT_FOUND);
		}

		return CouponResponse.from(coupon);
	}

	@Transactional
	public UserCouponResponse downloadCoupon(Long userId, Long couponId) {
		LocalDateTime now = LocalDateTime.now();

		Coupon coupon = couponRepository.findByCouponIdAndActiveTrue(couponId)
			.orElseThrow(() -> new CouponException(COUPON_NOT_FOUND));

		if (!coupon.isAvailable(now)) {
			throw new CouponException(COUPON_NOT_AVAILABLE);
		}

		if (userCouponRepository.existsByUserIdAndCoupon_CouponId(userId, couponId)) {
			throw new CouponException(COUPON_ALREADY_DOWNLOADED);
		}

		UserCoupon userCoupon = UserCoupon.create(userId, coupon, now);
		UserCoupon savedUserCoupon = userCouponRepository.save(userCoupon);

		return UserCouponResponse.from(savedUserCoupon, now);
	}

	public UserCouponPageResponse getMyCoupons(
		Long userId,
		Boolean used,
		Boolean expired,
		int page,
		int size
	) {
		validatePageRequest(page, size);

		LocalDateTime now = LocalDateTime.now();
		Pageable pageable = PageRequest.of(page, size);

		Page<UserCouponResponse> coupons = userCouponRepository.findMyCoupons(
			userId,
			used,
			expired,
			now,
			pageable
		).map(userCoupon -> UserCouponResponse.from(userCoupon, now));

		return UserCouponPageResponse.from(coupons);
	}

	public UserCouponResponse getMyCoupon(
		Long userId,
		Long userCouponId
	) {
		LocalDateTime now = LocalDateTime.now();

		UserCoupon userCoupon = userCouponRepository.findByUserCouponIdAndUserId(
			userCouponId,
			userId
		).orElseThrow(() -> new CouponException(USER_COUPON_NOT_FOUND));

		return UserCouponResponse.from(userCoupon, now);
	}

	public CouponDiscountResult applyCoupon(
		Long userId,
		Long userCouponId,
		int orderAmount
	) {
		if (userCouponId == null) {
			return CouponDiscountResult.none();
		}

		LocalDateTime now = LocalDateTime.now();

		UserCoupon userCoupon = userCouponRepository.findByUserCouponIdAndUserId(
			userCouponId,
			userId
		).orElseThrow(() -> new CouponException(USER_COUPON_NOT_FOUND));

		Coupon coupon = userCoupon.getCoupon();

		if (!coupon.isAvailable(now)) {
			throw new CouponException(COUPON_NOT_AVAILABLE);
		}

		if (userCoupon.isExpired(now)) {
			throw new CouponException(COUPON_EXPIRED);
		}

		if (userCoupon.isUsed()) {
			throw new CouponException(COUPON_ALREADY_USED);
		}

		if (orderAmount < coupon.getMinOrderAmount()) {
			throw new CouponException(COUPON_CONDITION_NOT_MET);
		}

		int discountAmount = calculateCouponDiscountAmount(coupon, orderAmount);

		return new CouponDiscountResult(coupon.getCouponId(), discountAmount);
	}

	@Transactional
	public void useCoupon(
		Long userId,
		Long userCouponId
	) {
		if (userCouponId == null) {
			return;
		}

		UserCoupon userCoupon = userCouponRepository.findByUserCouponIdAndUserId(
			userCouponId,
			userId
		).orElseThrow(() -> new CouponException(USER_COUPON_NOT_FOUND));

		userCoupon.use(LocalDateTime.now());
	}

	private int calculateCouponDiscountAmount(
		Coupon coupon,
		int orderAmount
	) {
		if (coupon.getDiscountType() == DiscountType.RATE) {
			return orderAmount * coupon.getDiscountValue() / 100;
		}

		return Math.min(coupon.getDiscountValue(), orderAmount);
	}
}