package com.princesses7.findy.recommendation.chatbot.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeIngredientRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeProductRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeRecommendationResponse;

@Component
public class ChatbotRecipeRecommendationPromptBuilder {

	public String build(ChatbotRecipeRecommendationResponse recipeRecommendation) {
		if (recipeRecommendation == null || !recipeRecommendation.hasData()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();

		builder.append("""
			Findy 레시피 재료별 상품 추천 데이터:
			아래 데이터는 사용자가 만들고 싶은 요리의 재료를 기준으로 실제 DB에서 조회한 상품이다.
			
			답변 지침:
			- 요리명과 필요한 재료를 먼저 짧게 안내한다.
			- 각 재료별로 추천 상품을 함께 안내한다.
			- 추천 상품이 없는 재료는 "추천 가능한 상품을 찾지 못했다"고 말한다.
			- 존재하지 않는 상품명, 가격, 재고를 지어내지 않는다.
			- 첫 번째 추천 상품은 우선 추천 상품으로 자연스럽게 안내한다.
			
			""");

		builder.append("요리명: ")
			.append(recipeRecommendation.recipeName())
			.append('\n');

		builder.append("매장 ID: ")
			.append(recipeRecommendation.storeId())
			.append('\n');

		for (ChatbotRecipeIngredientRecommendationResponse ingredient : recipeRecommendation.ingredients()) {
			builder.append("\n재료: ")
				.append(ingredient.ingredientName());

			if (ingredient.quantityText() != null && !ingredient.quantityText().isBlank()) {
				builder.append(" / 필요량: ")
					.append(ingredient.quantityText());
			}

			builder.append('\n');

			if (ingredient.recommendedProducts() == null || ingredient.recommendedProducts().isEmpty()) {
				builder.append("- 추천 상품 없음\n");
				continue;
			}

			for (ChatbotRecipeProductRecommendationResponse product : ingredient.recommendedProducts()) {
				builder.append("- ")
					.append(product.productName())
					.append(" / 판매가 ")
					.append(product.salePrice())
					.append("원");

				if (product.stockText() != null && !product.stockText().isBlank()) {
					builder.append(" / ")
						.append(product.stockText());
				}

				if (product.selected()) {
					builder.append(" / 우선 추천");
				}

				builder.append('\n');
			}
		}

		return builder.toString();
	}
}