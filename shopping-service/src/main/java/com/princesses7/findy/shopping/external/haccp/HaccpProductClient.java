package com.princesses7.findy.shopping.external.haccp;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductApiResponse;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class HaccpProductClient {

	private static final String SUCCESS_CODE = "00";
	private static final int DEFAULT_PAGE_NO = 1;
	private static final int DEFAULT_NUM_OF_ROWS = 10;

	private final WebClient webClient;
	private final HaccpProductProperties properties;
	private final ObjectMapper objectMapper;

	public HaccpProductClient(
		WebClient.Builder webClientBuilder,
		HaccpProductProperties properties,
		ObjectMapper objectMapper
	) {
		this.webClient = webClientBuilder
			.baseUrl(properties.baseUrl())
			.build();
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	public List<HaccpProductItemResponse> searchByProductName(String productName) {
		if (!StringUtils.hasText(productName)) {
			return List.of();
		}

		if (!StringUtils.hasText(properties.serviceKey())) {
			log.warn("HACCP service key is empty.");
			return List.of();
		}

		try {
			String rawResponse = webClient.get()
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
				.bodyToMono(String.class)
				.block();

			log.info("HACCP raw response={}", rawResponse);

			return parseItems(rawResponse);
		} catch (Exception exception) {
			log.warn("HACCP API request failed. productName={}, message={}", productName, exception.getMessage());
			return List.of();
		}
	}

	public Optional<HaccpProductItemResponse> findFirstByProductName(String productName) {
		return searchByProductName(productName)
			.stream()
			.findFirst();
	}

	private List<HaccpProductItemResponse> parseItems(String rawResponse) {
		if (!StringUtils.hasText(rawResponse)) {
			return List.of();
		}

		String trimmedResponse = rawResponse.trim();

		if (!trimmedResponse.startsWith("{")) {
			log.warn("HACCP API returned non-json response. response={}", trimmedResponse);
			return List.of();
		}

		try {
			HaccpProductApiResponse response = objectMapper.readValue(
				trimmedResponse,
				HaccpProductApiResponse.class
			);

			return extractItems(response);
		} catch (Exception exception) {
			log.warn("HACCP API response parse failed. message={}, response={}", exception.getMessage(),
				trimmedResponse);
			return List.of();
		}
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