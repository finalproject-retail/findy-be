package com.princesses7.findy.shopping.external.openai;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.princesses7.findy.shopping.global.config.OpenAiProperties;
import com.princesses7.findy.shopping.product.category.ProductCategoryCatalog;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;

import lombok.RequiredArgsConstructor;

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
		RestClient restClient = RestClient.builder()
			.baseUrl("https://api.openai.com")
			.build();

		Map<String, Object> request = createRequest(productName, brandName, externalCategory);

		try {
			JsonNode response = restClient.post()
				.uri("/v1/chat/completions")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(request)
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
			String categoryPath = result.path("categoryPath").asText();
			BigDecimal confidence = new BigDecimal(result.path("confidence").asText());
			String reason = result.path("reason").asText();

			return new ProductCategoryClassificationResponse(
				categoryId,
				categoryPath,
				confidence,
				confidence.compareTo(REVIEW_THRESHOLD) < 0,
				reason
			);
		} catch (Exception e) {
			return new ProductCategoryClassificationResponse(
				null,
				null,
				BigDecimal.ZERO,
				true,
				"AI 카테고리 분류에 실패했습니다."
			);
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
			"response_format", Map.of(
				"type", "json_object"
			)
		);
	}

	private String createSystemPrompt() {
		return """
			너는 대형마트 상품 카테고리 분류기다.
			반드시 제공된 Findy 카테고리 후보 중 하나만 선택한다.
			존재하지 않는 categoryId를 만들지 않는다.
			상품명, 브랜드, 외부 카테고리를 기준으로 가장 적절한 depth 3 카테고리를 고른다.
			confidence는 0.00부터 1.00 사이 숫자로 작성한다.
			응답은 반드시 JSON만 반환한다.
			
			응답 형식:
			{
			  "categoryId": 17,
			  "categoryPath": "가공/냉동 식품 > 면/통조림 > 라면",
			  "confidence": 0.95,
			  "reason": "상품명과 외부 카테고리에 라면 정보가 포함되어 있음"
			}
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
			createCategoryCandidateText()
		);
	}

	private String createCategoryCandidateText() {
		StringBuilder builder = new StringBuilder();

		for (ProductCategoryCatalog.CategoryCandidate category : ProductCategoryCatalog.CATEGORIES) {
			builder.append(category.categoryId())
				.append(": ")
				.append(category.path())
				.append("\n");
		}

		return builder.toString();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}