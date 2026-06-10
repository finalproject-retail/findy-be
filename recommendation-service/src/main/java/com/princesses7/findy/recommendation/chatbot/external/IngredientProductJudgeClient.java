package com.princesses7.findy.recommendation.chatbot.external;

import java.util.List;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeItem;

public interface IngredientProductJudgeClient {

	List<IngredientProductJudgeItem> judge(
		String recipeName,
		String ingredientName,
		List<ChatbotShoppingProduct> candidates,
		int limit
	);
}