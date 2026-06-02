package com.princesses7.findy.recommendation.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.notification.repository.NotificationRepository;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationDuplicateGuard {

	private static final long POPUP_COOLDOWN_MINUTES = 5;
	private static final long PRODUCT_COOLDOWN_MINUTES = 5;

	private final NotificationRepository notificationRepository;

	public boolean canShow(
		Long userId,
		Long shoppingListId,
		RecommendationNotificationCandidate candidate
	) {
		if (hasRecentPopup(userId)) {
			return false;
		}

		if (hasRecentSameProduct(userId, candidate.productId(), candidate.notificationType())) {
			return false;
		}

		return !hasSameShoppingListNotification(
			userId,
			shoppingListId,
			candidate.productId(),
			candidate.notificationType()
		);
	}

	private boolean hasRecentPopup(Long userId) {
		return notificationRepository.existsByUserIdAndSentAtAfter(
			userId,
			LocalDateTime.now().minusMinutes(POPUP_COOLDOWN_MINUTES)
		);
	}

	private boolean hasRecentSameProduct(
		Long userId,
		Long productId,
		NotificationType notificationType
	) {
		return notificationRepository.existsByUserIdAndProductIdAndNotificationTypeAndSentAtAfter(
			userId,
			productId,
			notificationType,
			LocalDateTime.now().minusMinutes(PRODUCT_COOLDOWN_MINUTES)
		);
	}

	private boolean hasSameShoppingListNotification(
		Long userId,
		Long shoppingListId,
		Long productId,
		NotificationType notificationType
	) {
		if (shoppingListId == null) {
			return false;
		}

		return notificationRepository.existsByUserIdAndShoppingListIdAndProductIdAndNotificationType(
			userId,
			shoppingListId,
			productId,
			notificationType
		);
	}
}