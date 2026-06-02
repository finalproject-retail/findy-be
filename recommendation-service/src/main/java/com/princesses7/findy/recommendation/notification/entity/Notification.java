package com.princesses7.findy.recommendation.notification.entity;

import java.time.LocalDateTime;

import com.princesses7.findy.recommendation.global.entity.BaseTimeEntity;
import com.princesses7.findy.recommendation.notification.support.NotificationDisplayPosition;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_id")
	private Long notificationId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 50)
	private NotificationType notificationType;

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "content", nullable = false, length = 500)
	private String content;

	@Column(name = "is_read", nullable = false)
	private boolean read;

	@Column(name = "product_id")
	private Long productId;

	@Column(name = "source_product_id")
	private Long sourceProductId;

	@Column(name = "promotion_id")
	private Long promotionId;

	@Column(name = "recommendation_log_id")
	private Long recommendationLogId;

	@Column(name = "store_id")
	private Long storeId;

	@Column(name = "shopping_list_id")
	private Long shoppingListId;

	@Enumerated(EnumType.STRING)
	@Column(name = "display_position", nullable = false, length = 50)
	private NotificationDisplayPosition displayPosition;

	@Column(name = "sent_at", nullable = false)
	private LocalDateTime sentAt;

	private Notification(
		Long userId,
		NotificationType notificationType,
		String title,
		String content,
		Long productId,
		Long sourceProductId,
		Long promotionId,
		Long recommendationLogId,
		Long storeId,
		Long shoppingListId,
		NotificationDisplayPosition displayPosition,
		LocalDateTime sentAt
	) {
		this.userId = userId;
		this.notificationType = notificationType;
		this.title = title;
		this.content = content;
		this.read = false;
		this.productId = productId;
		this.sourceProductId = sourceProductId;
		this.promotionId = promotionId;
		this.recommendationLogId = recommendationLogId;
		this.storeId = storeId;
		this.shoppingListId = shoppingListId;
		this.displayPosition = displayPosition;
		this.sentAt = sentAt;
	}

	public static Notification recommendation(
		Long userId,
		NotificationType notificationType,
		String title,
		String content,
		Long productId,
		Long sourceProductId,
		Long promotionId,
		Long recommendationLogId,
		Long storeId,
		Long shoppingListId,
		NotificationDisplayPosition displayPosition
	) {
		return new Notification(
			userId,
			notificationType,
			title,
			content,
			productId,
			sourceProductId,
			promotionId,
			recommendationLogId,
			storeId,
			shoppingListId,
			displayPosition,
			LocalDateTime.now()
		);
	}

	public void read() {
		this.read = true;
	}
}