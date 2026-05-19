package com.princesses7.findy.shopping.coupon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.coupon.dto.response.CouponPageResponse;
import com.princesses7.findy.shopping.coupon.dto.response.CouponResponse;
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

	@GetMapping("/{couponId}")
	public ApiResponse<CouponResponse> getAvailableCoupon(
		@PathVariable Long couponId
	) {
		CouponResponse response = couponService.getAvailableCoupon(couponId);

		return ApiResponse.ok(response);
	}
}