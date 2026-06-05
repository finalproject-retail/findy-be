package com.princesses7.findy.recommendation.notification.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.notification.repository.NotificationRepository;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationDuplicateGuard {

	private final NotificationRepository notificationRepository;

	public boolean canShow(
		Long userId,
		Long shoppingListId,
		RecommendationNotificationCandidate candidate
	) {
		return !hasSameShoppingListNotification(
			userId,
			shoppingListId,
			candidate.productId(),
			candidate.notificationType()
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