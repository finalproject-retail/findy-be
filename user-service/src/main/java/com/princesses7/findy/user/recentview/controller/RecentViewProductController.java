package com.princesses7.findy.user.recentview.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.recentview.dto.request.AddRecentViewProductRequest;
import com.princesses7.findy.user.recentview.dto.response.RecentViewProductListResponse;
import com.princesses7.findy.user.recentview.service.RecentViewProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/recent-views")
public class RecentViewProductController {

	private final RecentViewProductService recentViewProductService;

	@PostMapping
	public ApiResponse<Void> addRecentViewProduct(
		@RequestHeader("X-USER-ID") Long userId,
		@Valid @RequestBody AddRecentViewProductRequest request
	) {
		recentViewProductService.addRecentViewProduct(userId, request);

		return ApiResponse.ok("최근 본 상품 추가에 성공했습니다.");
	}

	@GetMapping
	public ApiResponse<RecentViewProductListResponse> getRecentViewProducts(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestParam(defaultValue = "20") int limit
	) {
		return ApiResponse.ok(
			"최근 본 상품 조회에 성공했습니다.",
			recentViewProductService.getRecentViewProducts(userId, limit)
		);
	}
}