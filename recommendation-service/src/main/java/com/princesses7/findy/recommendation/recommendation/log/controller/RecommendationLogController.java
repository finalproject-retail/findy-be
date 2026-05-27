package com.princesses7.findy.recommendation.recommendation.log.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationClickLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationLogResponse;
import com.princesses7.findy.recommendation.recommendation.log.service.RecommendationLogService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations/logs")
public class RecommendationLogController {

	private final RecommendationLogService recommendationLogService;

	@PostMapping("/clicks")
	public ApiResponse<RecommendationLogResponse> saveClickLog(
		@Valid @RequestBody RecommendationClickLogRequest request
	) {
		RecommendationLogResponse response = recommendationLogService.saveClickLog(request);

		return ApiResponse.ok("추천 클릭 로그 저장에 성공했습니다.", response);
	}
}