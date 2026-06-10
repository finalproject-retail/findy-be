package com.princesses7.findy.recommendation.chatbot.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientItem;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeIngredientRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeProductRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.repository.ShoppingProductReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotRecipeRecommendationService {

	private static final long DEFAULT_STORE_ID = 1L;
	private static final int PRODUCT_LIMIT_PER_INGREDIENT = 5;

	private final ChatbotRecipeIngredientExtractor recipeIngredientExtractor;
	private final ShoppingProductReadRepository shoppingProductReadRepository;

	public ChatbotRecipeRecommendationResponse recommend(
		Long userId,
		ChatbotMessageRequest request,
		ChatbotIntentAnalysis analysis
	) {
		if (analysis == null || analysis.intent() != ChatIntent.RECIPE_INGREDIENT_RECOMMENDATION) {
			return null;
		}

		Long storeId = normalizeStoreId(request.storeId());

		RecipeIngredientAnalysis recipeIngredientAnalysis = recipeIngredientExtractor.extract(
			request.message(),
			analysis.keyword()
		);

		if (recipeIngredientAnalysis.ingredients().isEmpty()) {
			return ChatbotRecipeRecommendationResponse.empty(
				recipeIngredientAnalysis.recipeName(),
				storeId
			);
		}

		List<ChatbotRecipeIngredientRecommendationResponse> ingredients = recipeIngredientAnalysis.ingredients()
			.stream()
			.map(ingredient -> recommendIngredient(userId, storeId, ingredient))
			.toList();

		return new ChatbotRecipeRecommendationResponse(
			recipeIngredientAnalysis.recipeName(),
			storeId,
			ingredients
		);
	}

	private ChatbotRecipeIngredientRecommendationResponse recommendIngredient(
		Long userId,
		Long storeId,
		RecipeIngredientItem ingredient
	) {
		List<ChatbotShoppingProduct> products = findProductsByIngredient(
			storeId,
			ingredient.ingredientName()
		);

		List<ChatbotRecipeProductRecommendationResponse> recommendedProducts = products.stream()
			.map(product -> ChatbotRecipeProductRecommendationResponse.from(
				userId,
				storeId,
				product,
				isFirstProduct(products, product)
			))
			.toList();

		return new ChatbotRecipeIngredientRecommendationResponse(
			ingredient.ingredientName(),
			ingredient.quantityText(),
			recommendedProducts
		);
	}

	private List<ChatbotShoppingProduct> findProductsByIngredient(
		Long storeId,
		String ingredientName
	) {
		if (ingredientName == null || ingredientName.isBlank()) {
			return List.of();
		}

		List<ChatbotShoppingProduct> products = shoppingProductReadRepository.searchByKeyword(
			ingredientName,
			storeId,
			PRODUCT_LIMIT_PER_INGREDIENT
		);

		Map<Long, ChatbotShoppingProduct> productMap = new LinkedHashMap<>();

		for (ChatbotShoppingProduct product : products) {
			if (product != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.productId(), product);
			}
		}

		return productMap.values()
			.stream()
			.limit(PRODUCT_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private boolean isFirstProduct(
		List<ChatbotShoppingProduct> products,
		ChatbotShoppingProduct product
	) {
		return !products.isEmpty()
			&& products.get(0).productId().equals(product.productId());
	}

	private Long normalizeStoreId(Long storeId) {
		if (storeId == null || storeId <= 0) {
			return DEFAULT_STORE_ID;
		}

		return storeId;
	}
}