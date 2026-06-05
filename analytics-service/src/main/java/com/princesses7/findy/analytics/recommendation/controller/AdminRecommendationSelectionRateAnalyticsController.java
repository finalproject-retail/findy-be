package com.princesses7.findy.analytics.recommendation.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationSelectionRateResponse;
import com.princesses7.findy.analytics.recommendation.service.RecommendationSelectionRateAnalyticsService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminRecommendationSelectionRateAnalyticsController {

	private final RecommendationSelectionRateAnalyticsService recommendationSelectionRateAnalyticsService;

	@GetMapping("/api/v1/admin/analytics/recommendations/selection-rate")
	public ApiResponse<RecommendationSelectionRateResponse> getRecommendationSelectionRateAnalytics(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam(required = false)
		String recommendationType,

		@RequestParam(required = false)
		Long productId,

		@RequestParam(required = false)
		Long sourceProductId,

		@RequestParam(required = false)
		@Min(1)
		@Max(100)
		Integer limit
	) {
		RecommendationSelectionRateResponse response =
			recommendationSelectionRateAnalyticsService.getSelectionRateAnalytics(
				fromDate,
				toDate,
				recommendationType,
				productId,
				sourceProductId,
				limit
			);

		return ApiResponse.ok("대체상품 및 행사상품 선택률 분석 조회에 성공했습니다.", response);
	}
}