package com.princesses7.findy.shopping.promotion.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.global.response.ApiResponse;
import com.princesses7.findy.shopping.promotion.dto.request.AddPromotionProductRequest;
import com.princesses7.findy.shopping.promotion.dto.request.CreatePromotionRequest;
import com.princesses7.findy.shopping.promotion.dto.request.UpdatePromotionRequest;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionDetailResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionPageResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionProductResponse;
import com.princesses7.findy.shopping.promotion.dto.response.PromotionResponse;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.service.PromotionAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/promotions")
public class AdminPromotionController {

	private final PromotionAdminService promotionAdminService;

	@PostMapping
	public ApiResponse<PromotionResponse> createPromotion(
		@Valid @RequestBody CreatePromotionRequest request
	) {
		PromotionResponse response = promotionAdminService.createPromotion(request);

		return ApiResponse.ok("프로모션 등록에 성공했습니다.", response);
	}

	@GetMapping
	public ApiResponse<PromotionPageResponse> getPromotions(
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) PromotionStatus status,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		PromotionPageResponse response = promotionAdminService.getPromotions(
			keyword,
			status,
			page,
			size
		);

		return ApiResponse.ok(response);
	}

	@GetMapping("/{promotionId}")
	public ApiResponse<PromotionDetailResponse> getPromotion(
		@PathVariable Long promotionId
	) {
		PromotionDetailResponse response = promotionAdminService.getPromotion(promotionId);

		return ApiResponse.ok(response);
	}

	@PatchMapping("/{promotionId}")
	public ApiResponse<PromotionResponse> updatePromotion(
		@PathVariable Long promotionId,
		@Valid @RequestBody UpdatePromotionRequest request
	) {
		PromotionResponse response = promotionAdminService.updatePromotion(
			promotionId,
			request
		);

		return ApiResponse.ok("프로모션 수정에 성공했습니다.", response);
	}

	@PatchMapping("/{promotionId}/end")
	public ApiResponse<Void> endPromotion(
		@PathVariable Long promotionId
	) {
		promotionAdminService.endPromotion(promotionId);

		return ApiResponse.ok("프로모션 종료에 성공했습니다.");
	}

	@PostMapping("/{promotionId}/products")
	public ApiResponse<PromotionProductResponse> addPromotionProduct(
		@PathVariable Long promotionId,
		@Valid @RequestBody AddPromotionProductRequest request
	) {
		PromotionProductResponse response = promotionAdminService.addPromotionProduct(
			promotionId,
			request
		);

		return ApiResponse.ok("프로모션 대상 상품 등록에 성공했습니다.", response);
	}

	@DeleteMapping("/{promotionId}/products/{promotionProductId}")
	public ApiResponse<Void> removePromotionProduct(
		@PathVariable Long promotionId,
		@PathVariable Long promotionProductId
	) {
		promotionAdminService.removePromotionProduct(
			promotionId,
			promotionProductId
		);

		return ApiResponse.ok("프로모션 대상 상품 삭제에 성공했습니다.");
	}
}