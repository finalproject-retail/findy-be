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
import com.princesses7.findy.recommendation.recommendation.log.dto.service.RecommendationImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.service.RecommendationLogService;
import com.princesses7.findy.recommendation.recommendation.service.PersonalizedRecommendationService;
import com.princesses7.findy.recommendation.recommendation.service.RelatedRecommendationService;
import com.princesses7.findy.recommendation.recommendation.service.SubstituteRecommendationService;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

	private static final String PERSONALIZED_DISPLAY_LOCATION = "HOME_PERSONALIZED";
	private static final String RELATED_DISPLAY_LOCATION = "PRODUCT_DETAIL_RELATED";
	private static final String SUBSTITUTE_DISPLAY_LOCATION = "SUBSTITUTE_RECOMMENDATION";

	private final PersonalizedRecommendationService personalizedRecommendationService;
	private final RelatedRecommendationService relatedRecommendationService;
	private final SubstituteRecommendationService substituteRecommendationService;
	private final RecommendationLogService recommendationLogService;

	@GetMapping("/personalized")
	public ApiResponse<PersonalizedRecommendationResponse> getPersonalizedRecommendations(
		@RequestParam Long userId,
		@RequestParam(defaultValue = "10") int size
	) {
		PersonalizedRecommendationResponse response = personalizedRecommendationService
			.getPersonalizedRecommendations(userId, size);

		saveImpressionLogsSafely(new RecommendationImpressionLogCommand(
			userId,
			null,
			null,
			RecommendationType.PERSONALIZED,
			PERSONALIZED_DISPLAY_LOCATION,
			response.recommendations()
		));

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

		saveImpressionLogsSafely(new RecommendationImpressionLogCommand(
			userId,
			productId,
			null,
			response.recommendationType(),
			RELATED_DISPLAY_LOCATION,
			response.recommendations()
		));

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

		saveImpressionLogsSafely(new RecommendationImpressionLogCommand(
			userId,
			productId,
			storeId,
			response.recommendationType(),
			SUBSTITUTE_DISPLAY_LOCATION,
			response.recommendations()
		));

		return ApiResponse.ok("대체 상품 추천 조회에 성공했습니다.", response);
	}

	private void saveImpressionLogsSafely(RecommendationImpressionLogCommand command) {
		try {
			recommendationLogService.saveImpressionLogs(command);
		} catch (Exception exception) {
			log.warn(
				"Recommendation impression log save failed. userId={}, recommendationType={}, displayLocation={}, message={}",
				command.userId(),
				command.recommendationType(),
				command.displayLocation(),
				exception.getMessage()
			);
		}
	}
}