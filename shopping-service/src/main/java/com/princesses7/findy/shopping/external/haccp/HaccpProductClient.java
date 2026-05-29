package com.princesses7.findy.shopping.external.haccp;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductApiResponse;
import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class HaccpProductClient {

	private static final String SUCCESS_CODE = "00";
	private static final String HACCP_PRODUCT_PATH = "/getCertImgListServiceV3";
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
				.uri(buildProductSearchUri(productName))
				.exchangeToMono(response -> response.bodyToMono(String.class)
					.map(body -> {
						if (response.statusCode().isError()) {
							log.warn(
								"HACCP API returned error. status={}, body={}",
								response.statusCode(),
								body
							);
						}
						return body;
					})
				)
				.block();

			log.info("HACCP raw response={}", rawResponse);

			return parseItems(rawResponse);
		} catch (Exception exception) {
			log.warn(
				"HACCP API request failed. productName={}, message={}",
				productName,
				exception.getMessage()
			);
			return List.of();
		}
	}

	public String getRawByProductName(String productName) {
		if (!StringUtils.hasText(productName)) {
			return "";
		}

		if (!StringUtils.hasText(properties.serviceKey())) {
			return "HACCP_SERVICE_KEY is empty.";
		}

		return webClient.get()
			.uri(buildProductSearchUri(productName))
			.retrieve()
			.bodyToMono(String.class)
			.block();
	}

	public Optional<HaccpProductItemResponse> findFirstByProductName(String productName) {
		return searchByProductName(productName)
			.stream()
			.findFirst();
	}

	private URI buildProductSearchUri(String productName) {
		String encodedProductName = UriUtils.encodeQueryParam(productName, StandardCharsets.UTF_8);

		String uri = HACCP_PRODUCT_PATH
			+ "?serviceKey=" + properties.serviceKey()
			+ "&returnType=json"
			+ "&pageNo=" + DEFAULT_PAGE_NO
			+ "&numOfRows=" + DEFAULT_NUM_OF_ROWS
			+ "&prdlstNm=" + encodedProductName;

		return URI.create(uri);
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
			log.warn(
				"HACCP API response parse failed. message={}, response={}",
				exception.getMessage(),
				trimmedResponse
			);
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