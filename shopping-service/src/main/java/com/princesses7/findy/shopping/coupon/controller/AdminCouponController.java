package com.princesses7.findy.shopping.coupon.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.coupon.dto.request.CreateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.response.CouponResponse;
import com.princesses7.findy.shopping.coupon.service.CouponService;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/coupons")
public class AdminCouponController {

	private final CouponService couponService;

	@PostMapping
	public ApiResponse<CouponResponse> createCoupon(
		@Valid @RequestBody CreateCouponRequest request
	) {
		CouponResponse response = couponService.createCoupon(request);

		return ApiResponse.ok("쿠폰 등록에 성공했습니다.", response);
	}
}