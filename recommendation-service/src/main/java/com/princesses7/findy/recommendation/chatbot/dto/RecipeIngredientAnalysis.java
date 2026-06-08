package com.princesses7.findy.recommendation.chatbot.dto;

import java.util.List;

public record RecipeIngredientAnalysis(
	String recipeName,
	List<RecipeIngredientItem> ingredients
) {

	public static RecipeIngredientAnalysis empty(String recipeName) {
		return new RecipeIngredientAnalysis(recipeName, List.of());
	}
}