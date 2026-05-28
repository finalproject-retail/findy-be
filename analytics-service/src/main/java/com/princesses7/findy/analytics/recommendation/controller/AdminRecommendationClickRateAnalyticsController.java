package com.princesses7.findy.analytics.recommendation.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.analytics.global.response.ApiResponse;
import com.princesses7.findy.analytics.recommendation.dto.response.RecommendationClickRateResponse;
import com.princesses7.findy.analytics.recommendation.service.RecommendationClickRateAnalyticsService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class AdminRecommendationClickRateAnalyticsController {

	private final RecommendationClickRateAnalyticsService recommendationClickRateAnalyticsService;

	@GetMapping("/api/v1/admin/analytics/recommendations/click-rate")
	public ApiResponse<RecommendationClickRateResponse> getRecommendationClickRateAnalytics(
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate fromDate,

		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		LocalDate toDate,

		@RequestParam(required = false)
		String recommendationType,

		@RequestParam(required = false)
		@Min(1)
		@Max(100)
		Integer limit
	) {
		RecommendationClickRateResponse response =
			recommendationClickRateAnalyticsService.getClickRateAnalytics(
				fromDate,
				toDate,
				recommendationType,
				limit
			);

		return ApiResponse.ok("추천 상품 클릭률 분석 조회에 성공했습니다.", response);
	}
}