package com.princesses7.findy.shopping.coupon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.coupon.dto.request.CreateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.request.UpdateCouponRequest;
import com.princesses7.findy.shopping.coupon.dto.response.CouponPageResponse;
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

	@GetMapping
	public ApiResponse<CouponPageResponse> getAdminCoupons(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) Boolean active,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		CouponPageResponse response = couponService.getAdminCoupons(
			keyword,
			active,
			page,
			size
		);

		return ApiResponse.ok(response);
	}

	@GetMapping("/{couponId}")
	public ApiResponse<CouponResponse> getAdminCoupon(
		@PathVariable Long couponId
	) {
		CouponResponse response = couponService.getAdminCoupon(couponId);

		return ApiResponse.ok(response);
	}

	@PatchMapping("/{couponId}")
	public ApiResponse<CouponResponse> updateCoupon(
		@PathVariable Long couponId,
		@Valid @RequestBody UpdateCouponRequest request
	) {
		CouponResponse response = couponService.updateCoupon(couponId, request);

		return ApiResponse.ok("쿠폰 수정에 성공했습니다.", response);
	}

	@PatchMapping("/{couponId}/deactivate")
	public ApiResponse<Void> deactivateCoupon(
		@PathVariable Long couponId
	) {
		couponService.deactivateCoupon(couponId);

		return ApiResponse.ok("쿠폰 비활성화에 성공했습니다.", null);
	}
}