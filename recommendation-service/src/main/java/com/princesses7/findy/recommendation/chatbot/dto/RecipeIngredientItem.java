package com.princesses7.findy.recommendation.chatbot.dto;

import java.util.List;

public record RecipeIngredientItem(
	String ingredientName,
	String quantityText,
	String searchKeyword,
	List<String> searchKeywords
) {

	public RecipeIngredientItem(
		String ingredientName,
		String quantityText
	) {
		this(ingredientName, quantityText, ingredientName, List.of(ingredientName));
	}

	public RecipeIngredientItem(
		String ingredientName,
		String quantityText,
		String searchKeyword
	) {
		this(ingredientName, quantityText, searchKeyword, List.of(searchKeyword));
	}

	public RecipeIngredientItem {
		ingredientName = normalizeText(ingredientName, "재료");
		quantityText = normalizeText(quantityText, "적당량");
		searchKeyword = normalizeText(searchKeyword, ingredientName);

		if (searchKeywords == null || searchKeywords.isEmpty()) {
			searchKeywords = List.of(searchKeyword);
		} else {
			searchKeywords = searchKeywords.stream()
				.filter(keyword -> keyword != null && !keyword.isBlank())
				.map(String::trim)
				.distinct()
				.limit(5)
				.toList();
		}
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