package com.princesses7.findy.recommendation.notification.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.notification.dto.response.NotificationProductResponse;
import com.princesses7.findy.recommendation.notification.support.NotificationType;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationListResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.service.RelatedRecommendationService;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatedProductNotificationService {

	private static final int CANDIDATE_SIZE = 5;

	private final RelatedRecommendationService relatedRecommendationService;
	private final NotificationMessageFactory messageFactory;

	public Optional<RecommendationNotificationCandidate> findCandidate(
		Long userId,
		Long sourceProductId,
		Collection<Long> excludeProductIds
	) {
		if (sourceProductId == null) {
			return Optional.empty();
		}

		ProductRecommendationListResponse response = relatedRecommendationService.getRelatedRecommendations(
			userId,
			sourceProductId,
			CANDIDATE_SIZE
		);

		return response.recommendations()
			.stream()
			.filter(recommendation -> !excludeProductIds.contains(recommendation.productId()))
			.findFirst()
			.map(recommendation -> toCandidate(response, recommendation));
	}

	private RecommendationNotificationCandidate toCandidate(
		ProductRecommendationListResponse response,
		ProductRecommendationResponse recommendation
	) {
		NotificationMessage message = messageFactory.createRelatedProductMessage(
			response.sourceProduct().productName(),
			recommendation
		);

		return new RecommendationNotificationCandidate(
			NotificationType.RELATED_PRODUCT_RECOMMENDATION,
			RecommendationType.RELATED,
			recommendation.productId(),
			response.sourceProductId(),
			null,
			BigDecimal.valueOf(recommendation.score()),
			recommendation.reason(),
			message,
			NotificationProductResponse.from(recommendation)
		);
	}
}