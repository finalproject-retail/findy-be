package com.princesses7.findy.shopping.promotion.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.promotion.dto.response.ApplicablePromotionResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionProductPageResponse;
import com.princesses7.findy.shopping.promotion.service.PromotionQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/promotions")
public class PromotionController {

	private final PromotionQueryService promotionQueryService;

	@GetMapping("/products/active")
	public ApiResponse<PromotionProductPageResponse> getActivePromotionProducts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		PromotionProductPageResponse response = promotionQueryService.getActivePromotionProducts(
			page,
			size
		);

		return ApiResponse.ok(response);
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