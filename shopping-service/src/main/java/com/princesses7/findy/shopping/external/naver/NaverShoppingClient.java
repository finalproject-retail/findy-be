package com.princesses7.findy.shopping.external.naver;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingResponse;
import com.princesses7.findy.shopping.global.config.NaverShoppingProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverShoppingClient {

	private final RestClient naverShoppingRestClient;
	private final NaverShoppingProperties properties;

	public NaverShoppingResponse search(String query, int display, int start) {
		return naverShoppingRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/v1/search/shop.json")
				.queryParam("query", query)
				.queryParam("display", display)
				.queryParam("start", start)
				.queryParam("sort", "sim")
				.build())
			.header("X-Naver-Client-Id", properties.clientId())
			.header("X-Naver-Client-Secret", properties.clientSecret())
			.retrieve()
			.body(NaverShoppingResponse.class);
	}
}