package com.princesses7.findy.recommendation.notification.dto.response;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.notification.entity.Notification;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

public record NotificationListItemResponse(
	Long notificationId,
	NotificationType notificationType,
	String title,
	String content,
	boolean isRead,
	Long productId,
	Long sourceProductId,
	Long promotionId,
	Long recommendationLogId,
	LocalDateTime sentAt
) {

	public static NotificationListItemResponse from(Notification notification) {
		return new NotificationListItemResponse(
			notification.getNotificationId(),
			notification.getNotificationType(),
			notification.getTitle(),
			notification.getContent(),
			notification.isRead(),
			notification.getProductId(),
			notification.getSourceProductId(),
			notification.getPromotionId(),
			notification.getRecommendationLogId(),
			notification.getSentAt()
		);
	}
}