package com.princesses7.findy.recommendation.recommendation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PersonalizedRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.service.PersonalizedRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

	private final PersonalizedRecommendationService personalizedRecommendationService;

	@GetMapping("/personalized")
	public ApiResponse<PersonalizedRecommendationResponse> getPersonalizedRecommendations(
		@RequestParam Long userId,
		@RequestParam(defaultValue = "10") int size
	) {
		PersonalizedRecommendationResponse response = personalizedRecommendationService
			.getPersonalizedRecommendations(userId, size);

		return ApiResponse.ok("개인 맞춤 추천 조회에 성공했습니다.", response);
	}
}