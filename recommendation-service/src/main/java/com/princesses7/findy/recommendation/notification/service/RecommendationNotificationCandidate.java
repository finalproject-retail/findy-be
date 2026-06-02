package com.princesses7.findy.recommendation.notification.service;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.notification.dto.response.NotificationProductResponse;
import com.princesses7.findy.recommendation.notification.support.NotificationType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

public record RecommendationNotificationCandidate(
	NotificationType notificationType,
	RecommendationType recommendationType,
	Long productId,
	Long sourceProductId,
	Long promotionId,
	BigDecimal score,
	String reason,
	NotificationMessage message,
	NotificationProductResponse product
) {
}