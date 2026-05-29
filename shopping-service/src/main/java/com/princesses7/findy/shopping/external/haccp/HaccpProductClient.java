package com.princesses7.findy.shopping.external.haccp;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriUtils;

import com.princesses7.findy.shopping.external.haccp.dto.response.HaccpProductItemResponse;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class HaccpProductClient {

	private static final String SUCCESS_CODE_OK = "OK";
	private static final String SUCCESS_CODE_00 = "00";
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

		try {
			return webClient.get()
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
		} catch (Exception exception) {
			log.warn(
				"HACCP raw API request failed. productName={}, message={}",
				productName,
				exception.getMessage()
			);

			return "HACCP raw API request failed: " + exception.getMessage();
		}
	}

	public Optional<HaccpProductItemResponse> findFirstByProductName(String productName) {
		return searchByProductName(productName)
			.stream()
			.findFirst();
	}

	private URI buildProductSearchUri(String productName) {
		String encodedProductName = UriUtils.encodeQueryParam(productName, StandardCharsets.UTF_8);

		String baseUrl = removeTrailingSlash(properties.baseUrl());

		String uri = baseUrl
			+ "/getCertImgListServiceV3"
			+ "?serviceKey=" + properties.serviceKey()
			+ "&returnType=json"
			+ "&pageNo=" + DEFAULT_PAGE_NO
			+ "&numOfRows=" + DEFAULT_NUM_OF_ROWS
			+ "&prdlstNm=" + encodedProductName;

		return URI.create(uri);
	}

	private String removeTrailingSlash(String value) {
		if (value.endsWith("/")) {
			return value.substring(0, value.length() - 1);
		}

		return value;
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
			JsonNode root = objectMapper.readTree(trimmedResponse);

			String resultCode = findText(root, "resultCode");
			String resultMessage = findText(root, "resultMessage");

			if (resultMessage == null) {
				resultMessage = findText(root, "resultMsg");
			}

			if (resultCode != null && !isSuccessResultCode(resultCode)) {
				log.warn("HACCP API failed. resultCode={}, resultMessage={}", resultCode, resultMessage);
				return List.of();
			}

			JsonNode itemsNode = findItemsNode(root);

			if (itemsNode == null || itemsNode.isNull() || itemsNode.isMissingNode()) {
				log.info("HACCP API item node is empty.");
				return List.of();
			}

			if (itemsNode.isArray()) {
				List<HaccpProductItemResponse> items = new ArrayList<>();

				for (JsonNode itemWrapperNode : itemsNode) {
					JsonNode itemNode = unwrapItemNode(itemWrapperNode);

					if (itemNode != null && !itemNode.isNull() && !itemNode.isMissingNode()) {
						items.add(toItem(itemNode));
					}
				}

				return items;
			}

			return List.of(toItem(unwrapItemNode(itemsNode)));
		} catch (Exception exception) {
			log.warn(
				"HACCP API response parse failed. message={}, response={}",
				exception.getMessage(),
				trimmedResponse
			);
			return List.of();
		}
	}

	private JsonNode findItemsNode(JsonNode root) {
		JsonNode directItems = root.path("body")
			.path("items");

		if (!directItems.isMissingNode() && !directItems.isNull()) {
			return directItems;
		}

		JsonNode responseItems = root.path("response")
			.path("body")
			.path("items");

		if (!responseItems.isMissingNode() && !responseItems.isNull()) {
			return responseItems;
		}

		return null;
	}

	private JsonNode unwrapItemNode(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return node;
		}

		JsonNode itemNode = node.path("item");

		if (!itemNode.isMissingNode() && !itemNode.isNull()) {
			return itemNode;
		}

		return node;
	}

	private HaccpProductItemResponse toItem(JsonNode itemNode) {
		return new HaccpProductItemResponse(
			text(itemNode, "prdlstReportNo", "PRDLST_REPORT_NO"),
			text(itemNode, "livestockFoodDiv", "LIVESTOCK_FOOD_DIV"),
			text(itemNode, "prdlstNm", "PRDLST_NM"),
			text(itemNode, "rawmtrl", "RAWMTRL"),
			text(itemNode, "allergy", "ALLERGY"),
			text(itemNode, "nutrient", "NUTRIENT"),
			text(itemNode, "barcode", "BARCODE", "BAR_CD"),
			text(itemNode, "prdkind", "PRDKIND"),
			text(itemNode, "prdkindstate", "PRDKINDSTATE"),
			text(itemNode, "manufacture", "MANUFACTURE"),
			text(itemNode, "seller", "SELLER"),
			text(itemNode, "capacity", "CAPACITY"),
			text(itemNode, "imgurl1", "IMGURL1"),
			text(itemNode, "imgurl2", "IMGURL2")
		);
	}

	private String findText(JsonNode root, String fieldName) {
		JsonNode direct = root.path("header")
			.path(fieldName);

		if (!direct.isMissingNode() && !direct.isNull()) {
			return direct.asText();
		}

		JsonNode response = root.path("response")
			.path("header")
			.path(fieldName);

		if (!response.isMissingNode() && !response.isNull()) {
			return response.asText();
		}

		return null;
	}

	private String text(JsonNode node, String... fieldNames) {
		for (String fieldName : fieldNames) {
			JsonNode value = node.path(fieldName);

			if (!value.isMissingNode() && !value.isNull()) {
				return value.asText();
			}
		}

		return null;
	}

	private boolean isSuccessResultCode(String resultCode) {
		return SUCCESS_CODE_OK.equalsIgnoreCase(resultCode)
			|| SUCCESS_CODE_00.equals(resultCode);
	}
}