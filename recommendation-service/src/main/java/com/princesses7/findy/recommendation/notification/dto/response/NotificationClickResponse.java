package com.princesses7.findy.recommendation.notification.dto.response;

import com.princesses7.findy.recommendation.notification.entity.Notification;

public record NotificationClickResponse(
	Long notificationId,
	Long productId,
	boolean isRead
) {

	public static NotificationClickResponse from(Notification notification) {
		return new NotificationClickResponse(
			notification.getNotificationId(),
			notification.getProductId(),
			notification.isRead()
		);
	}
}