package com.princesses7.findy.recommendation.external.bedrock;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeItem;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeResponse;
import com.princesses7.findy.recommendation.chatbot.external.IngredientProductJudgeClient;
import com.princesses7.findy.recommendation.global.config.BedrockProperties;

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
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "bedrock")
public class BedrockIngredientProductJudgeClient implements IngredientProductJudgeClient {

	private final BedrockRuntimeClient bedrockRuntimeClient;
	private final BedrockProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public List<IngredientProductJudgeItem> judge(
		String recipeName,
		String ingredientName,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}

		try {
			ConverseRequest request = ConverseRequest.builder()
				.modelId(properties.chatModelId())
				.system(SystemContentBlock.builder().text(systemPrompt()).build())
				.messages(Message.builder()
					.role(ConversationRole.USER)
					.content(ContentBlock.builder().text(userPrompt(
						recipeName,
						ingredientName,
						candidates,
						limit
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
				return List.of();
			}

			IngredientProductJudgeResponse judgeResponse = objectMapper.readValue(
				cleanJson(content),
				IngredientProductJudgeResponse.class
			);

			return judgeResponse.safeItems();
		} catch (Exception exception) {
			log.warn("Bedrock ingredient product judge failed. message={}", exception.getMessage(), exception);
			return List.of();
		}
	}

	private String systemPrompt() {
		return """
			당신은 대형마트 챗봇의 레시피 재료 상품 검수 모델입니다.
			
			목표:
			- 사용자가 특정 요리를 만들기 위해 ingredientName 재료를 찾고 있습니다.
			- candidateProduct가 해당 ingredientName 재료 자체로 사용할 수 있는 상품인지 판단하세요.
			
			판단 기준:
			- suitable=true: 후보 상품이 ingredientName 재료 자체로 사용 가능합니다.
			- suitable=false: 후보 상품이 재료 자체가 아니라 완제품, 밀키트, 소스 조합, 간식, 다른 요리, 해당 재료가 일부 들어간 가공식품입니다.
			
			규칙:
			- 상품명 키워드만 보고 판단하지 말고, 상품명/카테고리/브랜드/판매단위/중량 정보를 종합하세요.
			- ingredientName 재료가 일부 포함된 가공식품은 suitable=false입니다.
			- 특정 요리 완제품이나 밀키트는 ingredientName 재료 자체가 아니면 suitable=false입니다.
			- 후보에 없는 productId를 만들지 마세요.
			- confidence는 0.0 이상 1.0 이하로 주세요.
			- suitable=true 상품을 우선순위 높은 순서로 앞에 두세요.
			- 반드시 JSON 객체 하나만 반환하세요.
			- 마크다운 코드블록이나 설명 문장을 포함하지 마세요.
			""";
	}

	private String userPrompt(
		String recipeName,
		String ingredientName,
		List<ChatbotShoppingProduct> candidates,
		int limit
	) {
		return """
			요리명: %s
			찾는 재료명: %s
			
			후보 상품:
			%s
			
			요청:
			각 후보 상품이 "%s" 재료 자체로 사용 가능한 상품인지 판단하세요.
			suitable=true 상품만 최대 %d개가 최종 추천에 사용됩니다.
			
			응답 JSON 형식:
			{
			  "items": [
			    {
			      "productId": 123,
			      "suitable": true,
			      "confidence": 0.92,
			      "reason": "판단 이유"
			    }
			  ]
			}
			""".formatted(
			nullToEmpty(recipeName),
			nullToEmpty(ingredientName),
			createCandidateText(candidates),
			nullToEmpty(ingredientName),
			limit
		);
	}

	private String createCandidateText(List<ChatbotShoppingProduct> candidates) {
		StringBuilder builder = new StringBuilder();

		for (ChatbotShoppingProduct candidate : candidates) {
			builder.append("- productId: ")
				.append(candidate.productId())
				.append(", productName: ")
				.append(nullToEmpty(candidate.productName()))
				.append(", brandName: ")
				.append(nullToEmpty(candidate.brandName()))
				.append(", categoryName: ")
				.append(nullToEmpty(candidate.categoryName()))
				.append(", salesUnit: ")
				.append(nullToEmpty(candidate.salesUnit()))
				.append(", volume: ")
				.append(nullToEmpty(candidate.volume()))
				.append(", badgeText: ")
				.append(nullToEmpty(candidate.badgeText()))
				.append(", saleStatus: ")
				.append(nullToEmpty(candidate.saleStatus()))
				.append(", stockStatus: ")
				.append(nullToEmpty(candidate.stockStatus()))
				.append("\n");
		}

		return builder.toString();
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
			.collect(java.util.stream.Collectors.joining("\n"))
			.trim();
	}

	private String cleanJson(String content) {
		if (content == null || content.isBlank()) {
			return "{}";
		}

		return content
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}