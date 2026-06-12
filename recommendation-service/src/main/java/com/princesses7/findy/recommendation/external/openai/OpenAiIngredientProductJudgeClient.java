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
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ai", name = "provider", havingValue = "openai", matchIfMissing = true)
public class OpenAiIngredientProductJudgeClient implements IngredientProductJudgeClient {

	private static final int MAX_JUDGE_TOKENS = 500;

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
				Math.min(properties.maxTokens(), MAX_JUDGE_TOKENS)
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
			log.warn(
				"Ingredient product AI judge failed. recipeName={}, ingredientName={}, message={}",
				recipeName,
				ingredientName,
				exception.getMessage()
			);

			return List.of();
		}
	}

	private String systemPrompt() {
		return """
			너는 대형마트 챗봇의 레시피 재료 상품 검수 모델이다.
			
			목표:
			- 사용자는 특정 요리를 직접 만들기 위해 ingredientName 재료를 찾고 있다.
			- 후보 상품 중 ingredientName 재료 자체로 사용할 수 있는 상품만 골라야 한다.
			
			판단 기준:
			- suitable=true: 상품이 ingredientName 재료 자체다.
			- suitable=false: 완제품, 즉석식품, 레토르트, 밀키트, 요리 완성품, 도시락, 해당 재료가 일부 들어간 가공식품이다.
			
			엄격 제외 규칙:
			- recipeName 자체를 상품으로 만든 것은 무조건 suitable=false다.
			- 상품명에 재료명이 들어 있어도 재료 자체가 아니면 suitable=false다.
			- 김치찌개 요청에서 레토르트 김치찌개, 즉석 김치찌개, 김치찌개 밀키트는 suitable=false다.
			- 김치 재료에서 김치라면, 김치볶음밥, 김치만두, 김치찌개 완제품은 suitable=false다.
			- 돼지고기 재료에서 돼지고기김치찌개, 제육볶음 완제품, 돈육 가공 완제품은 suitable=false다.
			- 두부 재료에서 두부김치 완제품, 두부조림 완제품은 suitable=false다.
			
			허용 예시:
			- 김치: 포기김치, 배추김치, 맛김치, 썰은김치
			- 돼지고기: 찌개용 돼지고기, 앞다리살, 목살, 삼겹살
			- 두부: 찌개용 두부, 부침두부, 순두부
			- 대파: 대파, 손질대파
			- 양파: 양파, 깐양파
			- 마늘: 다진마늘, 깐마늘
			
			응답 규칙:
			- 후보 목록에 있는 productId만 반환한다.
			- suitable=true인 상품만 items에 넣는다.
			- 적합한 상품이 없으면 {"items": []}로 반환한다.
			- confidence는 0.0 이상 1.0 이하로 준다.
			- 반드시 JSON 객체만 반환한다.
			- 마크다운 코드블록, 설명 문장, 주석은 금지한다.
			
			응답 형식:
			{
			  "items": [
			    {
			      "productId": 123,
			      "suitable": true,
			      "confidence": 0.92,
			      "reason": "재료 자체 상품"
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
			완제품/즉석식품/레토르트/밀키트/요리 완성품은 절대 고르지 마.
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