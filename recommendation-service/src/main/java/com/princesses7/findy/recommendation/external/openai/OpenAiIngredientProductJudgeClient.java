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
				),
				Math.min(properties.maxTokens(), 500)
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
			- 후보 상품 목록 전체를 보고, 해당 ingredientName 재료 자체로 사용할 수 있는 상품만 골라낸다.
			
			가장 중요한 판단 기준:
			- suitable=true: 후보 상품이 ingredientName 재료 자체로 바로 조리에 사용할 수 있는 상품이다.
			- suitable=false: 후보 상품이 완제품, 즉석식품, 레토르트, 밀키트, 소스 조합, 간식, 다른 요리, 해당 재료가 일부 들어간 가공식품이다.
			
			엄격 제외 규칙:
			- 요리명과 같은 완제품은 반드시 제외한다.
			- 예: recipeName=김치찌개, ingredientName=김치일 때 레토르트 김치찌개, 즉석 김치찌개, 김치찌개 밀키트는 suitable=false이다.
			- 예: ingredientName=김치일 때 김치라면, 김치사발면, 김치볶음밥, 김치만두는 suitable=false이다.
			- 예: ingredientName=감자일 때 감자면, 감자칩, 감자스낵은 suitable=false이다.
			- 예: ingredientName=카레가루일 때 카레라면, 카레볶음밥, 카레 완제품은 suitable=false이다.
			- 상품명에 재료명이 들어 있어도 재료 자체가 아니면 반드시 suitable=false이다.
			
			허용 예시:
			- ingredientName=김치: 포기김치, 배추김치, 맛김치처럼 김치 자체 상품은 suitable=true이다.
			- ingredientName=두부: 찌개용 두부, 부침두부, 순두부처럼 두부 자체 상품은 suitable=true이다.
			- ingredientName=돼지고기: 삼겹살, 앞다리살, 목살처럼 돼지고기 원재료 상품은 suitable=true이다.
			- ingredientName=대파: 대파, 손질대파처럼 대파 자체 상품은 suitable=true이다.
			
			응답 규칙:
			- 후보 목록에 있는 productId만 응답한다.
			- 적합한 상품이 없으면 {"items": []}로 응답한다.
			- items에는 suitable=true라고 판단한 상품만 넣는다.
			- confidence는 0.0 이상 1.0 이하로 준다.
			- suitable=true 상품을 우선순위 높은 순서로 앞에 둔다.
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
			후보 상품 중 "%s" 재료 자체로 사용할 수 있는 상품만 최대 %d개 골라줘.
			완제품/즉석식품/레토르트/밀키트/요리명 자체 상품은 절대 고르지 마.
			적합한 상품이 없으면 {"items": []}만 반환해.
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