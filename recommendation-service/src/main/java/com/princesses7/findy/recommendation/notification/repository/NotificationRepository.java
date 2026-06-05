package com.princesses7.findy.recommendation.notification.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.notification.entity.Notification;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByUserIdOrderBySentAtDesc(Long userId, Pageable pageable);

	List<Notification> findByUserIdAndNotificationTypeOrderBySentAtDesc(
		Long userId,
		NotificationType notificationType,
		Pageable pageable
	);

	boolean existsByUserIdAndShoppingListIdAndProductIdAndNotificationType(
		Long userId,
		Long shoppingListId,
		Long productId,
		NotificationType notificationType
	);
}