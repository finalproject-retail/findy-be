package com.princesses7.findy.shopping.coupon.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record UserCouponPageResponse(
	List<UserCouponResponse> coupons,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {

	public static UserCouponPageResponse from(Page<UserCouponResponse> page) {
		return new UserCouponPageResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext()
		);
	}
}