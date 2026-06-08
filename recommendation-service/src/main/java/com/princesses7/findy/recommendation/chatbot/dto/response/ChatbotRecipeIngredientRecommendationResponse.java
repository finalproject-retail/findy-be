package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.util.List;

public record ChatbotRecipeIngredientRecommendationResponse(
	String ingredientName,
	String quantityText,
	List<ChatbotRecipeProductRecommendationResponse> recommendedProducts
) {
}