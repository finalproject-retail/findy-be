package com.princesses7.findy.recommendation.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.princesses7.findy.recommendation.notification.entity.Notification;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShoppingRecommendationNotificationResponse(
	boolean shouldShow,
	Long notificationId,
	Long recommendationLogId,
	NotificationType notificationType,
	String title,
	String content,
	NotificationProductResponse product
) {

	public static ShoppingRecommendationNotificationResponse empty() {
		return new ShoppingRecommendationNotificationResponse(
			false,
			null,
			null,
			null,
			null,
			null,
			null
		);
	}

	public static ShoppingRecommendationNotificationResponse of(
		Notification notification,
		NotificationProductResponse product
	) {
		return new ShoppingRecommendationNotificationResponse(
			true,
			notification.getNotificationId(),
			notification.getRecommendationLogId(),
			notification.getNotificationType(),
			notification.getTitle(),
			notification.getContent(),
			product
		);
	}
}