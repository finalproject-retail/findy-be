package com.princesses7.findy.shopping.coupon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.coupon.dto.response.AvailableOrderCouponResponse;
import com.princesses7.findy.shopping.coupon.dto.response.CouponPageResponse;
import com.princesses7.findy.shopping.coupon.dto.response.CouponResponse;
import com.princesses7.findy.shopping.coupon.dto.response.UserCouponPageResponse;
import com.princesses7.findy.shopping.coupon.dto.response.UserCouponResponse;
import com.princesses7.findy.shopping.coupon.service.CouponService;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/coupons")
public class CouponController {

	private final CouponService couponService;

	@GetMapping("/available")
	public ApiResponse<CouponPageResponse> getAvailableCoupons(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		CouponPageResponse response = couponService.getAvailableCoupons(page, size);

		return ApiResponse.ok(response);
	}

	@GetMapping("/available-for-order")
	public ApiResponse<List<AvailableOrderCouponResponse>> getAvailableOrderCoupons(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestParam int orderAmount
	) {
		return ApiResponse.ok(
			"주문 시 사용 가능한 쿠폰 조회에 성공했습니다.",
			couponService.getAvailableOrderCoupons(userId, orderAmount)
		);
	}

	@GetMapping("/{couponId}")
	public ApiResponse<CouponResponse> getAvailableCoupon(
		@PathVariable Long couponId
	) {
		CouponResponse response = couponService.getAvailableCoupon(couponId);

		return ApiResponse.ok(response);
	}

	@PostMapping("/{couponId}/download")
	public ApiResponse<UserCouponResponse> downloadCoupon(
		@RequestHeader("X-USER-ID") Long userId,
		@PathVariable Long couponId
	) {
		UserCouponResponse response = couponService.downloadCoupon(userId, couponId);

		return ApiResponse.ok("쿠폰 다운로드에 성공했습니다.", response);
	}

	@GetMapping("/me")
	public ApiResponse<UserCouponPageResponse> getMyCoupons(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestParam(required = false) Boolean used,
		@RequestParam(required = false) Boolean expired,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		UserCouponPageResponse response = couponService.getMyCoupons(
			userId,
			used,
			expired,
			page,
			size
		);

		return ApiResponse.ok(response);
	}

	@GetMapping("/me/{userCouponId}")
	public ApiResponse<UserCouponResponse> getMyCoupon(
		@RequestHeader("X-USER-ID") Long userId,
		@PathVariable Long userCouponId
	) {
		UserCouponResponse response = couponService.getMyCoupon(userId, userCouponId);

		return ApiResponse.ok(response);
	}
}