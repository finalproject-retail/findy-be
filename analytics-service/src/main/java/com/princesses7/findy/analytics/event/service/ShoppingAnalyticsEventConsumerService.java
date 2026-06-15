package com.princesses7.findy.analytics.event.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.event.dto.OrderLineItemPayload;
import com.princesses7.findy.analytics.event.dto.ProductViewSource;
import com.princesses7.findy.analytics.event.dto.RecommendationSource;
import com.princesses7.findy.analytics.event.dto.ShoppingAnalyticsEventPayload;
import com.princesses7.findy.analytics.event.repository.ShoppingAnalyticsEventAggregationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ShoppingAnalyticsEventConsumerService {

	private final ShoppingAnalyticsEventAggregationRepository aggregationRepository;
	private final ZoneId zoneId;

	public ShoppingAnalyticsEventConsumerService(
		ShoppingAnalyticsEventAggregationRepository aggregationRepository,
		@Value("${analytics.kafka.zone-id:Asia/Seoul}") String zoneId
	) {
		this.aggregationRepository = aggregationRepository;
		this.zoneId = ZoneId.of(zoneId);
	}

	@Transactional
	public void consume(ShoppingAnalyticsEventPayload event) {
		if (event == null || event.eventType() == null) {
			return;
		}

		boolean firstProcessing = aggregationRepository.markProcessed(
			event.eventId(),
			event.eventType().name()
		);

		if (!firstProcessing) {
			log.info("Skip already processed shopping analytics event. eventId={}", event.eventId());
			return;
		}

		switch (event.eventType()) {
			case PRODUCT_VIEWED -> handleProductViewed(event);
			case CART_ITEM_ADDED -> handleCartItemAdded(event);
			case SHOPPING_LIST_ITEM_ADDED -> handleShoppingListItemAdded(event);
			case ORDER_COMPLETED -> handleOrderCompleted(event);
		}
	}

	private void handleProductViewed(ShoppingAnalyticsEventPayload event) {
		LocalDate analysisDate = analysisDate(event.occurredAt());
		aggregationRepository.incrementProductView(
			event.productId(),
			event.categoryId(),
			analysisDate
		);

		if (event.viewSource() == ProductViewSource.MAP_PROMOTION) {
			recordRecommendationAction(
				event.userId(),
				event.productId(),
				null,
				RecommendationSource.PROMOTION,
				true,
				false,
				event.occurredAt(),
				analysisDate
			);
		}
	}

	private void handleCartItemAdded(ShoppingAnalyticsEventPayload event) {
		RecommendationSource source = normalizeRecommendationSource(event.recommendationSource());

		if (source == RecommendationSource.DIRECT) {
			return;
		}

		recordRecommendationAction(
			event.userId(),
			event.productId(),
			event.originalProductId(),
			source,
			true,
			false,
			event.occurredAt(),
			analysisDate(event.occurredAt())
		);
	}

	private void handleShoppingListItemAdded(ShoppingAnalyticsEventPayload event) {
		LocalDate analysisDate = analysisDate(event.occurredAt());
		List<Long> productIds = event.productIds() == null ? List.of() : event.productIds();

		for (Long productId : productIds) {
			aggregationRepository.incrementShoppingListProduct(productId, analysisDate, 1);
		}

		RecommendationSource source = normalizeRecommendationSource(event.recommendationSource());
		if (source == RecommendationSource.DIRECT) {
			return;
		}

		for (Long productId : productIds) {
			recordRecommendationAction(
				event.userId(),
				productId,
				event.originalProductId(),
				source,
				true,
				false,
				event.occurredAt(),
				analysisDate
			);
		}
	}

	private void handleOrderCompleted(ShoppingAnalyticsEventPayload event) {
		LocalDate analysisDate = analysisDate(event.occurredAt());
		aggregationRepository.incrementDailyPurchase(event.storeId(), analysisDate);

		List<OrderLineItemPayload> orderItems = event.orderItems() == null
			? List.of()
			: event.orderItems();

		for (OrderLineItemPayload orderItem : orderItems) {
			RecommendationSource source = normalizeRecommendationSource(orderItem.recommendationSource());
			if (source == RecommendationSource.DIRECT) {
				continue;
			}

			recordRecommendationAction(
				event.userId(),
				orderItem.productId(),
				orderItem.originalProductId(),
				source,
				true,
				true,
				event.occurredAt(),
				analysisDate
			);
		}
	}

	private void recordRecommendationAction(
		Long userId,
		Long productId,
		Long sourceProductId,
		RecommendationSource source,
		boolean clicked,
		boolean purchased,
		Instant occurredAt,
		LocalDate analysisDate
	) {
		String recommendationType = source.name();

		aggregationRepository.insertRecommendationLog(
			userId,
			productId,
			sourceProductId,
			recommendationType,
			clicked,
			purchased,
			eventTime(occurredAt)
		);

		if (clicked) {
			aggregationRepository.incrementRecommendationClickRate(
				recommendationType,
				productId,
				analysisDate,
				1,
				1
			);
		}

		aggregationRepository.incrementRecommendationConversion(
			recommendationType,
			productId,
			analysisDate,
			clicked ? 1 : 0,
			purchased ? 1 : 0
		);
	}

	private RecommendationSource normalizeRecommendationSource(RecommendationSource source) {
		return source == null ? RecommendationSource.DIRECT : source;
	}

	private LocalDate analysisDate(Instant occurredAt) {
		return eventTime(occurredAt).toLocalDate();
	}

	private LocalDateTime eventTime(Instant occurredAt) {
		Instant resolvedOccurredAt = occurredAt == null ? Instant.now() : occurredAt;
		return LocalDateTime.ofInstant(resolvedOccurredAt, zoneId);
	}
}
