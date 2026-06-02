package com.princesses7.findy.recommendation.notification.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.recommendation.global.response.ApiResponse;
import com.princesses7.findy.recommendation.notification.dto.response.NotificationClickResponse;
import com.princesses7.findy.recommendation.notification.dto.response.NotificationListItemResponse;
import com.princesses7.findy.recommendation.notification.dto.response.ShoppingRecommendationNotificationResponse;
import com.princesses7.findy.recommendation.notification.service.RecommendationNotificationService;
import com.princesses7.findy.recommendation.notification.support.NotificationType;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class RecommendationNotificationController {

	private final RecommendationNotificationService recommendationNotificationService;

	@GetMapping("/recommendations/shopping")
	public ApiResponse<ShoppingRecommendationNotificationResponse> getShoppingRecommendationNotification(
		@RequestParam Long userId,
		@RequestParam Long storeId,
		@RequestParam(required = false) Long shoppingListId,
		@RequestParam(required = false) Long sourceProductId,
		@RequestParam(required = false) Long currentGridId,
		@RequestParam(required = false) List<Long> excludeProductIds
	) {
		ShoppingRecommendationNotificationResponse response = recommendationNotificationService
			.createShoppingRecommendationNotification(
				userId,
				storeId,
				shoppingListId,
				sourceProductId,
				currentGridId,
				excludeProductIds
			);

		return ApiResponse.ok("쇼핑 중 추천 알림 조회에 성공했습니다.", response);
	}

	@GetMapping
	public ApiResponse<List<NotificationListItemResponse>> getNotifications(
		@RequestParam Long userId,
		@RequestParam(required = false) NotificationType notificationType,
		@RequestParam(defaultValue = "20") int size
	) {
		List<NotificationListItemResponse> response = recommendationNotificationService.getNotifications(
			userId,
			notificationType,
			size
		);

		return ApiResponse.ok("알림 목록 조회에 성공했습니다.", response);
	}

	@PatchMapping("/{notificationId}/click")
	public ApiResponse<NotificationClickResponse> clickNotification(
		@PathVariable Long notificationId,
		@RequestParam Long userId
	) {
		NotificationClickResponse response = recommendationNotificationService.clickNotification(
			userId,
			notificationId
		);

		return ApiResponse.ok("알림 클릭 처리에 성공했습니다.", response);
	}
}