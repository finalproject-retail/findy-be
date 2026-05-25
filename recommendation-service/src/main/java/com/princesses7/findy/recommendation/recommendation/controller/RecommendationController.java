package com.princesses7.findy.recommendation.recommendation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PersonalizedRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationListResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.SubstituteRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.service.PersonalizedRecommendationService;
import com.princesses7.findy.recommendation.recommendation.service.RelatedRecommendationService;
import com.princesses7.findy.recommendation.recommendation.service.SubstituteRecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

	private final PersonalizedRecommendationService personalizedRecommendationService;
	private final RelatedRecommendationService relatedRecommendationService;
	private final SubstituteRecommendationService substituteRecommendationService;

	@GetMapping("/personalized")
	public ApiResponse<PersonalizedRecommendationResponse> getPersonalizedRecommendations(
		@RequestParam Long userId,
		@RequestParam(defaultValue = "10") int size
	) {
		PersonalizedRecommendationResponse response = personalizedRecommendationService
			.getPersonalizedRecommendations(userId, size);

		return ApiResponse.ok("개인 맞춤 추천 조회에 성공했습니다.", response);
	}

	@GetMapping("/products/{productId}/related")
	public ApiResponse<ProductRecommendationListResponse> getRelatedRecommendations(
		@PathVariable Long productId,
		@RequestParam Long userId,
		@RequestParam(defaultValue = "5") int size
	) {
		ProductRecommendationListResponse response = relatedRecommendationService.getRelatedRecommendations(
			userId,
			productId,
			size
		);

		return ApiResponse.ok("연관 상품 추천 조회에 성공했습니다.", response);
	}

	@GetMapping("/products/{productId}/substitutes")
	public ApiResponse<SubstituteRecommendationResponse> getSubstituteRecommendations(
		@PathVariable Long productId,
		@RequestParam Long userId,
		@RequestParam Long storeId,
		@RequestParam(defaultValue = "5") int size
	) {
		SubstituteRecommendationResponse response = substituteRecommendationService.getSubstituteRecommendations(
			userId,
			productId,
			storeId,
			size
		);

		return ApiResponse.ok("대체 상품 추천 조회에 성공했습니다.", response);
	}
}