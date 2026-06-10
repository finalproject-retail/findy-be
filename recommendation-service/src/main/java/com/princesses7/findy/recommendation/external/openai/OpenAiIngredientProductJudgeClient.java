package com.princesses7.findy.recommendation.external.openai;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeItem;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeResponse;
import com.princesses7.findy.recommendation.chatbot.external.IngredientProductJudgeClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatRequest;
import com.princesses7.findy.recommendation.external.openai.dto.response.OpenAiChatResponse;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiIngredientProductJudgeClient implements IngredientProductJudgeClient {

	private final RestClient openAiRestClient;
	private final OpenAiProperties properties;
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
			OpenAiChatRequest request = OpenAiChatRequest.json(
				properties.chatModel(),
				List.of(
					OpenAiChatMessage.system(systemPrompt()),
					OpenAiChatMessage.user(userPrompt(recipeName, ingredientName, candidates, limit))
				)
			);

			OpenAiChatResponse response = openAiRestClient.post()
				.uri("/v1/chat/completions")
				.header("Authorization", "Bearer " + properties.apiKey())
				.header("Content-Type", "application/json")
				.body(request)
				.retrieve()
				.body(OpenAiChatResponse.class);

			if (response == null || response.firstContent().isBlank()) {
				return List.of();
			}

			IngredientProductJudgeResponse judgeResponse = objectMapper.readValue(
				cleanJson(response.firstContent()),
				IngredientProductJudgeResponse.class
			);

			return judgeResponse.safeItems();
		} catch (Exception exception) {
			return List.of();
		}
	}

	private String systemPrompt() {
		return """
			너는 대형마트 챗봇의 레시피 재료 상품 검수 모델이다.
			
			목표:
			- 사용자가 특정 요리를 만들기 위해 ingredientName 재료를 찾고 있다.
			- candidateProduct가 해당 ingredientName 재료 자체로 사용할 수 있는 상품인지 판단한다.
			
			판단 기준:
			- suitable=true: 후보 상품이 ingredientName 재료 자체로 사용 가능하다.
			- suitable=false: 후보 상품이 재료 자체가 아니라 완제품, 밀키트, 소스 조합, 간식, 다른 요리, 해당 재료가 일부 들어간 가공식품이다.
			
			중요 규칙:
			- 상품명 키워드만 보고 판단하지 말고, 상품명/카테고리/브랜드/설명/판매단위/중량 정보를 종합해서 판단한다.
			- ingredientName 재료가 일부 포함된 가공식품은 suitable=false이다.
			- 특정 요리 완제품이나 밀키트는 ingredientName 재료 자체가 아니면 suitable=false이다.
			- 후보 상품에 없는 productId를 만들지 않는다.
			- 후보 목록에 있는 productId만 응답한다.
			- confidence는 0.0 이상 1.0 이하로 준다.
			- suitable=true인 상품을 우선순위 높은 순서로 앞에 둔다.
			- 반드시 JSON 객체만 반환한다.
			- 마크다운 코드블록, 설명 문장, 주석을 포함하지 않는다.
			
			응답 형식:
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
			각 후보 상품이 "%s" 재료 자체로 사용 가능한 상품인지 판단해줘.
			suitable=true 상품만 최대 %d개가 최종 추천에 사용된다.
			단, 응답 items에는 판단한 후보들을 포함해도 된다.
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