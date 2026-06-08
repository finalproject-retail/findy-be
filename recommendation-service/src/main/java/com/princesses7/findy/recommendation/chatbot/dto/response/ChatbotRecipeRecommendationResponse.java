package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.util.List;

public record ChatbotRecipeRecommendationResponse(
	String recipeName,
	Long storeId,
	List<ChatbotRecipeIngredientRecommendationResponse> ingredients
) {

	public boolean hasData() {
		return ingredients != null && !ingredients.isEmpty();
	}

	public static ChatbotRecipeRecommendationResponse empty(
		String recipeName,
		Long storeId
	) {
		return new ChatbotRecipeRecommendationResponse(
			recipeName,
			storeId,
			List.of()
		);
	}
}