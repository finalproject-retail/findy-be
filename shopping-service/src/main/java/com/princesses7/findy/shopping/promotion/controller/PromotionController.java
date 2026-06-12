package com.princesses7.findy.shopping.promotion.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.promotion.dto.response.ApplicablePromotionResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionMapMarkerListResponse;
import com.princesses7.findy.shopping.promotion.service.PromotionQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/promotions")
public class PromotionController {

	private final PromotionQueryService promotionQueryService;

	@GetMapping("/products/active")
	public ApiResponse<PromotionMapMarkerListResponse> getActivePromotionProducts() {
		PromotionMapMarkerListResponse response = promotionQueryService.getActivePromotionMapMarkers();

		return ApiResponse.ok("진행 중인 행사 상품 지도 마커 조회에 성공했습니다.", response);
	}

	@GetMapping("/products/{productId}/applicable")
	public ApiResponse<List<ApplicablePromotionResponse>> getApplicablePromotions(
		@PathVariable Long productId
	) {
		List<ApplicablePromotionResponse> response = promotionQueryService
			.getApplicablePromotions(productId);

		return ApiResponse.ok(response);
	}
}