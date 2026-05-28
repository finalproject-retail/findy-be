package com.princesses7.findy.shopping.external.openai;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.global.config.OpenAiProperties;
import com.princesses7.findy.shopping.product.category.ProductCategoryCatalog;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiCategoryClassifierClient {

	private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("0.85");

	private final OpenAiProperties properties;
	private final ObjectMapper objectMapper;

	public ProductCategoryClassificationResponse classify(
		String productName,
		String brandName,
		String externalCategory
	) {
		if (properties.apiKey() == null || properties.apiKey().isBlank()) {
			return ProductCategoryClassificationResponse.failed("OpenAI API Key가 설정되지 않았습니다.");
		}

		RestClient restClient = RestClient.builder()
			.baseUrl("https://api.openai.com")
			.build();

		try {
			JsonNode response = restClient.post()
				.uri("/v1/chat/completions")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(createRequest(productName, brandName, externalCategory))
				.retrieve()
				.body(JsonNode.class);

			String content = response
				.path("choices")
				.get(0)
				.path("message")
				.path("content")
				.asText();

			JsonNode result = objectMapper.readTree(content);

			Long categoryId = result.path("categoryId").asLong();
			BigDecimal confidence = new BigDecimal(result.path("confidence").asText("0"));
			String reason = result.path("reason").asText("AI 카테고리 분류 결과입니다.");

			if (!ProductCategoryCatalog.exists(categoryId)) {
				return ProductCategoryClassificationResponse.failed("AI가 존재하지 않는 카테고리를 반환했습니다.");
			}

			String categoryPath = ProductCategoryCatalog.pathOf(categoryId);

			return new ProductCategoryClassificationResponse(
				categoryId,
				categoryPath,
				confidence,
				confidence.compareTo(REVIEW_THRESHOLD) < 0,
				reason
			);
		} catch (Exception exception) {
			log.warn("AI 카테고리 분류에 실패했습니다. productName={}, brandName={}, externalCategory={}",
				productName,
				brandName,
				externalCategory,
				exception
			);

			return ProductCategoryClassificationResponse.failed("AI 카테고리 분류에 실패했습니다.");
		}
	}

	private Map<String, Object> createRequest(
		String productName,
		String brandName,
		String externalCategory
	) {
		return Map.of(
			"model", properties.model(),
			"temperature", 0,
			"messages", List.of(
				Map.of(
					"role", "system",
					"content", createSystemPrompt()
				),
				Map.of(
					"role", "user",
					"content", createUserPrompt(productName, brandName, externalCategory)
				)
			),
			"response_format", createResponseFormat()
		);
	}

	private Map<String, Object> createResponseFormat() {
		return Map.of(
			"type", "json_schema",
			"json_schema", Map.of(
				"name", "product_category_classification",
				"strict", true,
				"schema", Map.of(
					"type", "object",
					"additionalProperties", false,
					"properties", Map.of(
						"categoryId", Map.of(
							"type", "integer",
							"enum", ProductCategoryCatalog.categoryIds()
						),
						"categoryPath", Map.of(
							"type", "string"
						),
						"confidence", Map.of(
							"type", "number",
							"minimum", 0,
							"maximum", 1
						),
						"reason", Map.of(
							"type", "string"
						)
					),
					"required", List.of(
						"categoryId",
						"categoryPath",
						"confidence",
						"reason"
					)
				)
			)
		);
	}

	private String createSystemPrompt() {
		return """
			너는 대형마트 상품 카테고리 분류기다.
			반드시 제공된 Findy 카테고리 후보 중 하나만 선택한다.
			존재하지 않는 categoryId를 만들지 않는다.
			상품명, 브랜드, 외부 카테고리를 기준으로 가장 적절한 depth 3 카테고리를 고른다.
			상품명이 쇼핑몰 광고 문구를 포함하더라도 실제 상품의 종류를 우선 판단한다.
			확신이 낮아도 가장 가까운 카테고리 하나를 선택하되 confidence를 낮게 준다.
			응답은 반드시 지정된 JSON Schema를 따른다.
			""";
	}

	private String createUserPrompt(
		String productName,
		String brandName,
		String externalCategory
	) {
		return """
			상품명: %s
			브랜드: %s
			외부 카테고리: %s
			
			Findy 카테고리 후보:
			%s
			""".formatted(
			nullToEmpty(productName),
			nullToEmpty(brandName),
			nullToEmpty(externalCategory),
			ProductCategoryCatalog.createCandidateText()
		);
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}