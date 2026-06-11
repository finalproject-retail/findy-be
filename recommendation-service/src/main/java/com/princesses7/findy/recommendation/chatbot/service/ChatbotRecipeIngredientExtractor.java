package com.princesses7.findy.recommendation.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientItem;
import com.princesses7.findy.recommendation.external.openai.OpenAiChatClient;
import com.princesses7.findy.recommendation.external.openai.dto.request.OpenAiChatMessage;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ChatbotRecipeIngredientExtractor {

	private final OpenAiChatClient openAiChatClient;
	private final ObjectMapper objectMapper;

	public RecipeIngredientAnalysis extract(
		String message,
		String keyword
	) {
		String recipeName = normalizeRecipeName(keyword, message);

		try {
			String response = openAiChatClient.jsonChat(List.of(
				OpenAiChatMessage.system(systemPrompt()),
				OpenAiChatMessage.user(message)
			));

			LlmRecipeIngredientResult result = objectMapper.readValue(
				cleanJson(response),
				LlmRecipeIngredientResult.class
			);

			return new RecipeIngredientAnalysis(
				normalizeRecipeName(result.recipeName(), recipeName),
				normalizeIngredients(result.ingredients())
			);
		} catch (Exception exception) {
			return RecipeIngredientAnalysis.empty(recipeName);
		}
	}

	private List<RecipeIngredientItem> normalizeIngredients(
		List<LlmRecipeIngredientItem> ingredients
	) {
		if (ingredients == null || ingredients.isEmpty()) {
			return List.of();
		}

		return ingredients.stream()
			.filter(ingredient -> hasText(ingredient.ingredientName()))
			.map(ingredient -> new RecipeIngredientItem(
				ingredient.ingredientName().trim(),
				normalizeQuantityText(ingredient.quantityText())
			))
			.distinct()
			.limit(12)
			.toList();
	}

	private String normalizeRecipeName(
		String value,
		String fallback
	) {
		if (hasText(value)) {
			return value.trim();
		}

		if (hasText(fallback)) {
			return fallback.trim();
		}

		return "요리";
	}

	private String normalizeQuantityText(String quantityText) {
		if (!hasText(quantityText)) {
			return "적당량";
		}

		return quantityText.trim();
	}

	private String cleanJson(String response) {
		if (response == null || response.isBlank()) {
			return "{}";
		}

		return response
			.replace("```json", "")
			.replace("```", "")
			.trim();
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private String systemPrompt() {
		return """
			너는 요리 재료 추출기이다.
			
			사용자의 메시지에서 만들고 싶은 요리명과 필요한 장보기 재료를 JSON으로만 반환한다.
			설명 문장, 마크다운, 코드블록은 포함하지 않는다.
			
			응답 형식:
			{
			  "recipeName": "요리명",
			  "ingredients": [
			    {
			      "ingredientName": "재료명",
			      "quantityText": "구매 수량"
			    }
			  ]
			}
			
			규칙:
			- ingredientName에는 마트에서 검색 가능한 실제 조리 재료명만 넣는다.
			- 상품명, 브랜드명, 완제품명, 컵라면명, 스낵명은 넣지 않는다.
			- 사용자가 요리 재료를 요청한 경우 완제품이나 즉석식품이 아니라 조리에 필요한 재료를 추출한다.
			- 소금, 후추, 물처럼 일반적으로 구매 대상이 아닌 기본 재료는 제외한다.
			- 사용자가 명시적으로 구매 의도를 말한 기본 재료는 포함할 수 있다.
			- 너무 세부적인 조리 표현은 제외한다.
			- 최대 12개까지만 반환한다.
			""";
	}

	private record LlmRecipeIngredientResult(
		String recipeName,
		List<LlmRecipeIngredientItem> ingredients
	) {
	}

	private record LlmRecipeIngredientItem(
		String ingredientName,
		String quantityText
	) {
	}
}