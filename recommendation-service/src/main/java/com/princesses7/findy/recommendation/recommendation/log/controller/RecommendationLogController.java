package com.princesses7.findy.recommendation.recommendation.log.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationClickLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationImpressionLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationPurchaseConversionRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationSelectionLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationLogResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationPurchaseConversionResponse;
import com.princesses7.findy.recommendation.recommendation.log.service.RecommendationLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations/logs")
public class RecommendationLogController {

	private final RecommendationLogService recommendationLogService;

	@PostMapping
	public ApiResponse<RecommendationLogResponse> saveImpressionLog(
		@Valid @RequestBody RecommendationImpressionLogRequest request
	) {
		RecommendationLogResponse response = recommendationLogService.saveImpressionLog(request);

		return ApiResponse.ok("추천 노출 로그 저장에 성공했습니다.", response);
	}

	@PostMapping("/clicks")
	public ApiResponse<RecommendationLogResponse> saveClickLog(
		@Valid @RequestBody RecommendationClickLogRequest request
	) {
		RecommendationLogResponse response = recommendationLogService.saveClickLog(request);

		return ApiResponse.ok("추천 클릭 로그 저장에 성공했습니다.", response);
	}

	@PostMapping("/substitute-selections")
	public ApiResponse<RecommendationLogResponse> saveSubstituteSelectionLog(
		@Valid @RequestBody RecommendationSelectionLogRequest request
	) {
		RecommendationLogResponse response = recommendationLogService.saveSelectionLog(request);

		return ApiResponse.ok("대체상품 선택 로그 저장에 성공했습니다.", response);
	}

	@PatchMapping("/purchase-conversions")
	public ApiResponse<RecommendationPurchaseConversionResponse> savePurchaseConversionLog(
		@Valid @RequestBody RecommendationPurchaseConversionRequest request
	) {
		RecommendationPurchaseConversionResponse response = recommendationLogService.savePurchaseConversionLog(request);

		return ApiResponse.ok("추천 구매 전환 로그 저장에 성공했습니다.", response);
	}
}