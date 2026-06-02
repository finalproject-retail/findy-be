package com.princesses7.findy.shopping.recentview.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.recentview.dto.response.RecentViewProductListResponse;
import com.princesses7.findy.shopping.recentview.service.RecentViewProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
// TODO: user-service로 이동
@RequestMapping("/api/v1/users/me/recent-views")
public class RecentViewProductController {

	private final RecentViewProductService recentViewProductService;

	@GetMapping
	public ApiResponse<RecentViewProductListResponse> getRecentViewProducts(
		@RequestHeader("X-User-Id") Long userId,
		@RequestParam(defaultValue = "20") int limit
	) {
		return ApiResponse.ok(
			"최근 본 상품 조회에 성공했습니다.",
			recentViewProductService.getRecentViewProducts(userId, limit)
		);
	}
}