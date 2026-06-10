package com.princesses7.findy.recommendation.chatbot.dto;

import java.util.List;

public record IngredientProductJudgeResponse(
	List<IngredientProductJudgeItem> items
) {

	public List<IngredientProductJudgeItem> safeItems() {
		if (items == null) {
			return List.of();
		}

		return items;
	}
}