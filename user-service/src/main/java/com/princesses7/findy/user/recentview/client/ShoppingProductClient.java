package com.princesses7.findy.user.recentview.client;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.recentview.dto.response.ProductSummaryResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShoppingProductClient {

	private final RestClient restClient;

	public ShoppingProductClient(
		@Value("${services.shopping.base-url:http://localhost:8887}") String shoppingServiceBaseUrl
	) {
		this.restClient = RestClient.builder()
			.baseUrl(shoppingServiceBaseUrl)
			.build();
	}

	public Map<Long, ProductSummaryResponse> getProductSummaryMap(List<Long> productIds) {
		if (productIds == null || productIds.isEmpty()) {
			return Map.of();
		}

		try {
			ApiResponse<List<ProductSummaryResponse>> response = restClient.get()
				.uri(uriBuilder -> uriBuilder
					.path("/internal/products/summaries")
					.queryParam("productIds", productIds.toArray())
					.build()
				)
				.retrieve()
				.body(new ParameterizedTypeReference<>() {
				});

			if (response == null || response.data() == null) {
				return Map.of();
			}

			return response.data()
				.stream()
				.collect(Collectors.toMap(
					ProductSummaryResponse::productId,
					Function.identity(),
					(existingProduct, replacementProduct) -> existingProduct
				));
		} catch (RestClientException exception) {
			log.warn(
				"Failed to get product summaries. productIds={}, message={}",
				productIds,
				exception.getMessage()
			);

			return Map.of();
		}
	}
}