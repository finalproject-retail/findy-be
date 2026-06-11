package com.princesses7.findy.shopping.recommendation.client;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.princesses7.findy.shopping.order.dto.request.OrderRecommendationSourceRequest;
import com.princesses7.findy.shopping.recommendation.dto.request.RecommendationPurchaseConversionRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RecommendationLogClient {

	private final RestClient recommendationServiceRestClient;

	public RecommendationLogClient(
		@Qualifier("recommendationServiceRestClient") RestClient recommendationServiceRestClient
	) {
		this.recommendationServiceRestClient = recommendationServiceRestClient;
	}

	public void sendPurchaseConversion(
		Long userId,
		Long orderId,
		Collection<Long> purchasedProductIds,
		List<OrderRecommendationSourceRequest> recommendationSources
	) {
		List<Long> recommendationLogIds = recommendationSources.stream()
			.map(OrderRecommendationSourceRequest::recommendationLogId)
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		List<Long> normalizedPurchasedProductIds = purchasedProductIds.stream()
			.filter(Objects::nonNull)
			.distinct()
			.toList();

		if (recommendationLogIds.isEmpty() || normalizedPurchasedProductIds.isEmpty()) {
			return;
		}

		try {
			recommendationServiceRestClient.patch()
				.uri("/api/v1/recommendations/logs/purchase-conversions")
				.body(new RecommendationPurchaseConversionRequest(
					userId,
					orderId,
					recommendationLogIds,
					normalizedPurchasedProductIds
				))
				.retrieve()
				.toBodilessEntity();
		} catch (RestClientException exception) {
			log.warn(
				"Recommendation purchase conversion request failed. userId={}, orderId={}, message={}",
				userId,
				orderId,
				exception.getMessage()
			);
		}
	}
}