package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.global.config.OpenAiProperties;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchedBy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiProductMatchScorer {

	private final OpenAiProperties properties;
	private final ObjectMapper objectMapper;

	public ProductMatchResult score(
		Product product,
		KcaProductPriceItemResponse externalProduct,
		ProductMatchResult fallback
	) {
		if (!hasApiKey() || externalProduct.goodName() == null || externalProduct.goodName().isBlank()) {
			return fallback;
		}

		try {
			RestClient restClient = RestClient.builder()
				.baseUrl("https://api.openai.com")
				.build();

			JsonNode response = restClient.post()
				.uri("/v1/chat/completions")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(createRequest(product, externalProduct, fallback))
				.retrieve()
				.body(JsonNode.class);

			String content = response
				.path("choices")
				.get(0)
				.path("message")
				.path("content")
				.asText();

			JsonNode result = objectMapper.readTree(content);

			BigDecimal confidence = new BigDecimal(result.path("confidence").asText("0"))
				.setScale(4, RoundingMode.HALF_UP);

			String reason = result.path("reason").asText("AI 동일 상품 판단 결과입니다.");

			return new ProductMatchResult(
				confidence,
				ProductExternalMatchedBy.AI,
				reason
			);
		} catch (Exception exception) {
			log.warn(
				"AI 상품 매칭 판단에 실패했습니다. productName={}, kcaGoodName={}",
				product.getProductName(),
				externalProduct.goodName(),
				exception
			);

			return fallback;
		}
	}

	private boolean hasApiKey() {
		return properties.apiKey() != null && !properties.apiKey().isBlank();
	}

	private Map<String, Object> createRequest(
		Product product,
		KcaProductPriceItemResponse externalProduct,
		ProductMatchResult fallback
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
					"content", createUserPrompt(product, externalProduct, fallback)
				)
			),
			"response_format", createResponseFormat()
		);
	}

	private Map<String, Object> createResponseFormat() {
		return Map.of(
			"type", "json_schema",
			"json_schema", Map.of(
				"name", "product_match_judgement",
				"strict", true,
				"schema", Map.of(
					"type", "object",
					"additionalProperties", false,
					"properties", Map.of(
						"sameProduct", Map.of(
							"type", "boolean"
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
						"sameProduct",
						"confidence",
						"reason"
					)
				)
			)
		);
	}

	private String createSystemPrompt() {
		return """
			너는 대형마트 상품 매칭 검수 담당자다.
			두 상품이 같은 상품인지 판단한다.
			가격은 판단 기준으로 사용하지 않는다.
			상품명, 브랜드명, 제조사명, 용량, 중량, 판매 단위를 기준으로 판단한다.
			동일 상품이면 높은 confidence를 준다.
			비슷한 종류지만 용량/브랜드가 다르면 낮은 confidence를 준다.
			응답은 반드시 JSON Schema를 따른다.
			""";
	}

	private String createUserPrompt(
		Product product,
		KcaProductPriceItemResponse externalProduct,
		ProductMatchResult fallback
	) {
		return """
			[우리 DB 상품]
			상품명: %s
			브랜드명: %s
			용량/중량: %s
			판매 단위: %s
			
			[한국소비자원 상품]
			상품명: %s
			제조사명: %s
			상품 ID: %s
			
			[룰 기반 사전 점수]
			confidence: %s
			
			두 상품이 같은 상품인지 판단해줘.
			""".formatted(
			nullToEmpty(product.getProductName()),
			nullToEmpty(product.getBrandName()),
			nullToEmpty(product.getVolume()),
			nullToEmpty(product.getSalesUnit()),
			nullToEmpty(externalProduct.goodName()),
			nullToEmpty(externalProduct.productEntpName()),
			nullToEmpty(externalProduct.goodId()),
			fallback.confidence()
		);
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}