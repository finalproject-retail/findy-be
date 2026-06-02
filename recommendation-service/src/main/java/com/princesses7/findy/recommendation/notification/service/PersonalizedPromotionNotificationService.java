package com.princesses7.findy.recommendation.notification.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.notification.dto.response.NotificationProductResponse;
import com.princesses7.findy.recommendation.notification.support.NotificationType;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.service.PromotionRecommendationService;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedPromotionNotificationService {

	private static final int CANDIDATE_SIZE = 5;

	private final PromotionRecommendationService promotionRecommendationService;
	private final NotificationMessageFactory messageFactory;

	public Optional<RecommendationNotificationCandidate> findCandidate(
		Long userId,
		Long storeId,
		Long currentGridId,
		Collection<Long> excludeProductIds
	) {
		PromotionRecommendationResponse response = promotionRecommendationService.getPromotionRecommendations(
			userId,
			storeId,
			CANDIDATE_SIZE,
			currentGridId
		);

		return response.recommendations()
			.stream()
			.filter(recommendation -> !excludeProductIds.contains(recommendation.productId()))
			.findFirst()
			.map(this::toCandidate);
	}

	private RecommendationNotificationCandidate toCandidate(PromotionProductRecommendationResponse recommendation) {
		NotificationMessage message = messageFactory.createPersonalizedPromotionMessage(recommendation);

		return new RecommendationNotificationCandidate(
			NotificationType.PERSONALIZED_PROMOTION_RECOMMENDATION,
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			recommendation.productId(),
			null,
			recommendation.promotionId(),
			BigDecimal.valueOf(recommendation.score()),
			recommendation.reason(),
			message,
			NotificationProductResponse.from(recommendation)
		);
	}
}