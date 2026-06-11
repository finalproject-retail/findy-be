package com.princesses7.findy.recommendation.chatbot.dto;

public record RecipeIngredientItem(
	String ingredientName,
	String quantityText,
	String searchKeyword
) {

	public RecipeIngredientItem(
		String ingredientName,
		String quantityText
	) {
		this(ingredientName, quantityText, ingredientName);
	}

	public RecipeIngredientItem {
		ingredientName = normalizeText(ingredientName, "재료");
		quantityText = normalizeText(quantityText, "적당량");
		searchKeyword = normalizeText(searchKeyword, ingredientName);
	}

	private static String normalizeText(
		String value,
		String fallback
	) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		return value.trim();
	}
}