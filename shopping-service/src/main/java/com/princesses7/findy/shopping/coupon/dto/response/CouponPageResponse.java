package com.princesses7.findy.shopping.coupon.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record CouponPageResponse(
	List<CouponResponse> coupons,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {

	public static CouponPageResponse from(Page<CouponResponse> page) {
		return new CouponPageResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext()
		);
	}
}