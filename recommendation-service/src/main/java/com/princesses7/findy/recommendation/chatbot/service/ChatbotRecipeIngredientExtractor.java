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
				normalizeQuantityText(ingredient.quantityText()),
				normalizeSearchKeyword(
					ingredient.searchKeyword(),
					ingredient.ingredientName()
				),
				normalizeSearchKeywords(
					ingredient.searchKeywords(),
					ingredient.searchKeyword(),
					ingredient.ingredientName()
				)
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

	private String normalizeSearchKeyword(
		String searchKeyword,
		String ingredientName
	) {
		if (hasText(searchKeyword)) {
			return searchKeyword.trim();
		}

		if (hasText(ingredientName)) {
			return ingredientName.trim();
		}

		return "재료";
	}

	private List<String> normalizeSearchKeywords(
		List<String> searchKeywords,
		String searchKeyword,
		String ingredientName
	) {
		if (searchKeywords != null && !searchKeywords.isEmpty()) {
			return searchKeywords.stream()
				.filter(this::hasText)
				.map(String::trim)
				.distinct()
				.limit(5)
				.toList();
		}

		if (hasText(searchKeyword)) {
			return List.of(searchKeyword.trim());
		}

		if (hasText(ingredientName)) {
			return List.of(ingredientName.trim());
		}

		return List.of("재료");
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
			너는 대형마트 장보기용 요리 재료 추출기이다.
			
			사용자의 메시지에서 만들고 싶은 요리명과 필요한 장보기 재료를 JSON으로만 반환한다.
			설명 문장, 마크다운, 코드블록은 포함하지 않는다.
			
			응답 형식:
			{
			  "recipeName": "요리명",
			  "ingredients": [
			    {
			      "ingredientName": "화면에 보여줄 재료명",
			      "searchKeyword": "가장 대표적인 상품 검색 키워드",
			      "searchKeywords": ["상품 DB에서 검색해볼 키워드1", "상품 DB에서 검색해볼 키워드2"],
			      "quantityText": "구매 수량"
			    }
			  ]
			}
			
			필드 규칙:
			- ingredientName은 사용자에게 보여줄 재료명이다.
			- searchKeyword는 가장 대표적인 상품 검색 키워드이다.
			- searchKeywords는 같은 재료를 찾기 위해 마트 상품 DB에서 함께 검색해볼 수 있는 표현들이다.
			- searchKeywords에는 ingredientName, 손질 형태, 포장 상품명에 가까운 표현을 포함할 수 있다.
			- 예: ingredientName이 "마늘"이면 searchKeywords는 ["마늘", "깐마늘", "다진마늘"]처럼 줄 수 있다.
			- 예: ingredientName이 "돼지등뼈"이면 searchKeywords는 ["돼지등뼈", "돼지고기"]처럼 줄 수 있다.
			- 예: ingredientName이 "감자"이면 searchKeywords는 ["감자"]처럼 준다.
			- 예: ingredientName이 "국간장"이면 searchKeywords는 ["국간장"]처럼 준다.
			
			재료 추출 규칙:
			- ingredientName에는 마트에서 구매 가능한 실제 조리 재료명만 넣는다.
			- 상품명, 브랜드명, 완제품명, 컵라면명, 스낵명은 넣지 않는다.
			- 사용자가 요리 재료를 요청한 경우 완제품이나 즉석식품이 아니라 조리에 필요한 재료를 추출한다.
			- 소금, 후추, 물처럼 일반적으로 집에 있는 기본 재료는 제외한다.
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
		String searchKeyword,
		List<String> searchKeywords,
		String quantityText
	) {
	}
}