package com.princesses7.findy.recommendation.notification.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;
import com.princesses7.findy.recommendation.notification.dto.response.NotificationClickResponse;
import com.princesses7.findy.recommendation.notification.dto.response.NotificationListItemResponse;
import com.princesses7.findy.recommendation.notification.dto.response.ShoppingRecommendationNotificationResponse;
import com.princesses7.findy.recommendation.notification.entity.Notification;
import com.princesses7.findy.recommendation.notification.repository.NotificationRepository;
import com.princesses7.findy.recommendation.notification.support.NotificationDisplayPosition;
import com.princesses7.findy.recommendation.notification.support.NotificationType;
import com.princesses7.findy.recommendation.recommendation.log.dto.request.RecommendationClickLogRequest;
import com.princesses7.findy.recommendation.recommendation.log.dto.response.RecommendationLogResponse;
import com.princesses7.findy.recommendation.recommendation.log.dto.service.RecommendationSingleImpressionLogCommand;
import com.princesses7.findy.recommendation.recommendation.log.service.RecommendationLogService;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationNotificationService {

	private static final String SHOPPING_POPUP_DISPLAY_LOCATION = "SHOPPING_POPUP";
	private static final int DEFAULT_NOTIFICATION_SIZE = 20;
	private static final int MAX_NOTIFICATION_SIZE = 50;

	private final RelatedProductNotificationService relatedProductNotificationService;
	private final PersonalizedPromotionNotificationService personalizedPromotionNotificationService;
	private final NotificationDuplicateGuard duplicateGuard;
	private final NotificationRepository notificationRepository;
	private final RecommendationLogService recommendationLogService;
	private final RecommendationRequestValidator requestValidator;

	@Transactional
	public ShoppingRecommendationNotificationResponse createShoppingRecommendationNotification(
		Long userId,
		Long storeId,
		Long shoppingListId,
		Long sourceProductId,
		Long currentGridId,
		List<Long> excludeProductIds
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(storeId, "storeId");

		Set<Long> normalizedExcludeProductIds = normalizeExcludeProductIds(excludeProductIds, sourceProductId);

		Optional<RecommendationNotificationCandidate> relatedCandidate = relatedProductNotificationService.findCandidate(
			userId,
			sourceProductId,
			normalizedExcludeProductIds
		);

		if (relatedCandidate.isPresent()) {
			Optional<ShoppingRecommendationNotificationResponse> response = createIfDisplayable(
				userId,
				storeId,
				shoppingListId,
				relatedCandidate.get()
			);

			if (response.isPresent()) {
				return response.get();
			}
		}

		Optional<RecommendationNotificationCandidate> promotionCandidate = personalizedPromotionNotificationService.findCandidate(
			userId,
			storeId,
			currentGridId,
			normalizedExcludeProductIds
		);

		return promotionCandidate
			.flatMap(candidate -> createIfDisplayable(userId, storeId, shoppingListId, candidate))
			.orElseGet(ShoppingRecommendationNotificationResponse::empty);
	}

	public List<NotificationListItemResponse> getNotifications(
		Long userId,
		NotificationType notificationType,
		int size
	) {
		requestValidator.validatePositiveId(userId, "userId");
		int normalizedSize = requestValidator.normalizeSize(size, DEFAULT_NOTIFICATION_SIZE, MAX_NOTIFICATION_SIZE);
		PageRequest pageRequest = PageRequest.of(0, normalizedSize);

		if (notificationType == null) {
			return notificationRepository.findByUserIdOrderBySentAtDesc(userId, pageRequest)
				.stream()
				.map(NotificationListItemResponse::from)
				.toList();
		}

		return notificationRepository.findByUserIdAndNotificationTypeOrderBySentAtDesc(
				userId,
				notificationType,
				pageRequest
			)
			.stream()
			.map(NotificationListItemResponse::from)
			.toList();
	}

	@Transactional
	public NotificationClickResponse clickNotification(
		Long userId,
		Long notificationId
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(notificationId, "notificationId");

		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND));

		if (!notification.getUserId().equals(userId)) {
			throw new BaseException(ErrorCode.NOTIFICATION_NOT_FOUND);
		}

		notification.read();

		recommendationLogService.saveClickLog(
			new RecommendationClickLogRequest(
				notification.getUserId(),
				notification.getProductId(),
				notification.getSourceProductId(),
				notification.getStoreId(),
				toRecommendationType(notification.getNotificationType()),
				SHOPPING_POPUP_DISPLAY_LOCATION,
				1,
				null
			)
		);

		return NotificationClickResponse.from(notification);
	}

	private Optional<ShoppingRecommendationNotificationResponse> createIfDisplayable(
		Long userId,
		Long storeId,
		Long shoppingListId,
		RecommendationNotificationCandidate candidate
	) {
		if (!duplicateGuard.canShow(userId, shoppingListId, candidate)) {
			return Optional.empty();
		}

		RecommendationLogResponse logResponse = recommendationLogService.saveSingleImpressionLog(
			new RecommendationSingleImpressionLogCommand(
				userId,
				candidate.productId(),
				candidate.sourceProductId(),
				storeId,
				candidate.recommendationType(),
				SHOPPING_POPUP_DISPLAY_LOCATION,
				1,
				candidate.score(),
				candidate.reason()
			)
		);

		Notification notification = Notification.recommendation(
			userId,
			candidate.notificationType(),
			candidate.message().title(),
			candidate.message().content(),
			candidate.productId(),
			candidate.sourceProductId(),
			candidate.promotionId(),
			logResponse.recommendationLogId(),
			storeId,
			shoppingListId,
			NotificationDisplayPosition.SHOPPING_POPUP
		);

		Notification savedNotification = notificationRepository.save(notification);

		return Optional.of(
			ShoppingRecommendationNotificationResponse.of(savedNotification, candidate.product())
		);
	}

	private Set<Long> normalizeExcludeProductIds(
		Collection<Long> excludeProductIds,
		Long sourceProductId
	) {
		Set<Long> normalized = new HashSet<>();

		if (excludeProductIds != null) {
			excludeProductIds.stream()
				.filter(productId -> productId != null && productId > 0)
				.forEach(normalized::add);
		}

		if (sourceProductId != null && sourceProductId > 0) {
			normalized.add(sourceProductId);
		}

		return normalized;
	}

	private RecommendationType toRecommendationType(NotificationType notificationType) {
		if (notificationType == NotificationType.PERSONALIZED_PROMOTION_RECOMMENDATION) {
			return RecommendationType.AI_PERSONALIZED_PROMOTION;
		}

		return RecommendationType.RELATED;
	}
}