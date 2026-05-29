package com.princesses7.findy.shopping.external.haccp;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductApiResponse;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HaccpProductClient {

	private static final String SUCCESS_CODE = "00";
	private static final int DEFAULT_PAGE_NO = 1;
	private static final int DEFAULT_NUM_OF_ROWS = 10;

	private final WebClient webClient;
	private final HaccpProductProperties properties;

	public HaccpProductClient(
		WebClient.Builder webClientBuilder,
		HaccpProductProperties properties
	) {
		this.webClient = webClientBuilder
			.baseUrl(properties.baseUrl())
			.build();
		this.properties = properties;
	}

	public List<HaccpProductItemResponse> searchByProductName(String productName) {
		if (!StringUtils.hasText(productName)) {
			return List.of();
		}

		HaccpProductApiResponse response = webClient.get()
			.uri(uriBuilder -> uriBuilder
				.path("/getCertImgListServiceV3")
				.queryParam("serviceKey", properties.serviceKey())
				.queryParam("returnType", "json")
				.queryParam("pageNo", DEFAULT_PAGE_NO)
				.queryParam("numOfRows", DEFAULT_NUM_OF_ROWS)
				.queryParam("prdlstNm", productName)
				.build()
			)
			.retrieve()
			.bodyToMono(HaccpProductApiResponse.class)
			.block();

		return extractItems(response);
	}

	public Optional<HaccpProductItemResponse> findFirstByProductName(String productName) {
		return searchByProductName(productName)
			.stream()
			.findFirst();
	}

	private List<HaccpProductItemResponse> extractItems(HaccpProductApiResponse response) {
		if (response == null) {
			return List.of();
		}

		if (response.header() != null && !SUCCESS_CODE.equals(response.header().resultCode())) {
			log.warn(
				"HACCP API failed. resultCode={}, resultMessage={}",
				response.header().resultCode(),
				response.header().resultMessage()
			);
			return List.of();
		}

		if (response.body() == null || response.body().items() == null) {
			return List.of();
		}

		return response.body().items();
	}
}