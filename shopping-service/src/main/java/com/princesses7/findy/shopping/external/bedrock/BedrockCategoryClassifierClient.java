package com.princesses7.findy.shopping.external.bedrock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.princesses7.findy.shopping.external.ai.CategoryClassifierClient;
import com.princesses7.findy.shopping.global.config.BedrockProperties;
import com.princesses7.findy.shopping.product.category.ProductCategoryCatalog;
import com.princesses7.findy.shopping.product.dto.response.ProductCategoryClassificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlock;
import software.amazon.awssdk.services.bedrockruntime.model.ConversationRole;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.InferenceConfiguration;
import software.amazon.awssdk.services.bedrockruntime.model.Message;
import software.amazon.awssdk.services.bedrockruntime.model.SystemContentBlock;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "bedrock")
public class BedrockCategoryClassifierClient implements CategoryClassifierClient {

	private static final BigDecimal REVIEW_THRESHOLD = new BigDecimal("0.85");

	private final BedrockRuntimeClient bedrockRuntimeClient;
	private final BedrockProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public ProductCategoryClassificationResponse classify(
		String productName,
		String brandName,
		String externalCategory
	) {
		try {
			ConverseRequest request = ConverseRequest.builder()
				.modelId(properties.chatModelId())
				.system(SystemContentBlock.builder().text(createSystemPrompt()).build())
				.messages(Message.builder()
					.role(ConversationRole.USER)
					.content(ContentBlock.builder().text(createUserPrompt(
						productName,
						brandName,
						externalCategory
					)).build())
					.build())
				.inferenceConfig(InferenceConfiguration.builder()
					.maxTokens(properties.maxTokens())
					.temperature(properties.temperature())
					.build())
				.build();

			ConverseResponse response = bedrockRuntimeClient.converse(request);
			String content = extractText(response);

			if (content.isBlank()) {
				return ProductCategoryClassificationResponse.failed("Bedrock category classification returned empty content.");
			}

			JsonNode result = objectMapper.readTree(cleanJson(content));

			Long categoryId = result.path("categoryId").isMissingNode() ? null : result.path("categoryId").asLong();
			BigDecimal confidence = new BigDecimal(result.path("confidence").asText("0"));
			String reason = result.path("reason").asText("Bedrock category classification result.");

			if (!ProductCategoryCatalog.exists(categoryId)) {
				return ProductCategoryClassificationResponse.failed("Bedrock returned an unknown categoryId.");
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
			log.warn("Bedrock category classification failed. productName={}, brandName={}, externalCategory={}",
				productName,
				brandName,
				externalCategory,
				exception
			);

			return ProductCategoryClassificationResponse.failed("Bedrock category classification failed.");
		}
	}

	private String createSystemPrompt() {
		return """
			너는 대형마트 상품 카테고리 분류기다.
			반드시 제공된 Findy 카테고리 후보 중 하나만 선택한다.
			존재하지 않는 categoryId를 만들지 않는다.
			상품명, 브랜드, 외부 카테고리를 기준으로 가장 적절한 depth 3 카테고리를 고른다.
			상품명이 쇼핑몰 광고 문구를 포함하더라도 실제 상품의 종류를 우선 판단한다.
			확신이 낮아도 가장 가까운 카테고리 하나를 선택하되 confidence를 낮게 준다.
			응답은 반드시 JSON 객체 하나만 반환한다.
			markdown, 코드블록, 설명 문장은 절대 포함하지 않는다.
			JSON 객체는 반드시 categoryId, categoryPath, confidence, reason 필드를 포함해야 한다.
			confidence는 0.0 이상 1.0 이하의 숫자여야 한다.
			categoryPath는 제공된 후보의 경로를 그대로 사용한다.
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

		응답 JSON 형식:
		{
		  "categoryId": 10,
		  "categoryPath": "Findy 카테고리 후보에서 복사한 카테고리 경로",
		  "confidence": 0.92,
		  "reason": "분류 이유를 짧게 작성"
			}
			""".formatted(
			nullToEmpty(productName),
			nullToEmpty(brandName),
			nullToEmpty(externalCategory),
			ProductCategoryCatalog.createCandidateText()
		);
	}

	private String extractText(ConverseResponse response) {
		if (response == null || response.output() == null || response.output().message() == null) {
			return "";
		}

		return response.output()
			.message()
			.content()
			.stream()
			.map(ContentBlock::text)
			.filter(Objects::nonNull)
			.collect(Collectors.joining("\n"))
			.trim();
	}

	private String cleanJson(String content) {
		return content
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
