package com.princesses7.findy.recommendation.external.shopping;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.external.shopping.dto.FrequentPurchaseProductListResponse;
import com.princesses7.findy.recommendation.external.shopping.dto.FrequentPurchaseProductResponse;
import com.princesses7.findy.recommendation.external.shopping.dto.ShoppingApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PurchaseHistoryClient {

	private static final int DEFAULT_LIMIT = 20;

	private final RestClient shoppingRestClient;

	public PurchaseHistoryClient(@Qualifier("shoppingRestClient") RestClient shoppingRestClient) {
		this.shoppingRestClient = shoppingRestClient;
	}

	public List<FrequentPurchaseProductResponse> findFrequentPurchaseProducts(Long userId) {
		try {
			ShoppingApiResponse<FrequentPurchaseProductListResponse> response = shoppingRestClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/api/v1/orders/stats/frequent-products")
					.queryParam("limit", DEFAULT_LIMIT)
					.build())
				.header("X-USER-ID", String.valueOf(userId))
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			if (response == null || !response.success() || response.data() == null || response.data().products() == null) {
				return List.of();
			}

			return response.data().products();
		} catch (Exception exception) {
			log.warn("Failed to fetch purchase history from shopping-service. userId={}, message={}",
				userId,
				exception.getMessage()
			);
			return List.of();
		}
	}
}
