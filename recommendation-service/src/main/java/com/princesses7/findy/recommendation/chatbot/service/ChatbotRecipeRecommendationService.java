package com.princesses7.findy.recommendation.chatbot.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.ChatbotShoppingProduct;
import com.princesses7.findy.recommendation.chatbot.dto.IngredientProductJudgeItem;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientItem;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeIngredientRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeProductRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.chatbot.external.IngredientProductJudgeClient;
import com.princesses7.findy.recommendation.chatbot.repository.ShoppingProductReadRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotRecipeRecommendationService {

	private static final long DEFAULT_STORE_ID = 1L;
	private static final int PRODUCT_LIMIT_PER_INGREDIENT = 3;
	private static final int AI_CANDIDATE_POOL_LIMIT = 30;
	private static final int MAX_MATCHING_INGREDIENT_COUNT = 6;
	private static final double MIN_JUDGE_CONFIDENCE = 0.60;

	private final ChatbotRecipeIngredientExtractor recipeIngredientExtractor;
	private final ShoppingProductReadRepository shoppingProductReadRepository;
	private final IngredientProductJudgeClient ingredientProductJudgeClient;

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

		List<ChatbotShoppingProduct> aiJudgeCandidates = shoppingProductReadRepository.findIngredientJudgeCandidates(
			storeId,
			AI_CANDIDATE_POOL_LIMIT
		);

		List<ChatbotRecipeIngredientRecommendationResponse> ingredients = recipeIngredientAnalysis.ingredients()
			.stream()
			.limit(MAX_MATCHING_INGREDIENT_COUNT)
			.map(ingredient -> recommendIngredient(
				userId,
				storeId,
				recipeIngredientAnalysis.recipeName(),
				ingredient,
				aiJudgeCandidates
			))
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
		String recipeName,
		RecipeIngredientItem ingredient,
		List<ChatbotShoppingProduct> aiJudgeCandidates
	) {
		List<ChatbotShoppingProduct> products = findProductsByIngredient(
			recipeName,
			ingredient,
			aiJudgeCandidates
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
		String recipeName,
		RecipeIngredientItem ingredient,
		List<ChatbotShoppingProduct> aiJudgeCandidates
	) {
		if (ingredient == null || !hasText(ingredient.ingredientName())) {
			return List.of();
		}

		if (aiJudgeCandidates == null || aiJudgeCandidates.isEmpty()) {
			return List.of();
		}

		String ingredientName = ingredient.ingredientName().trim();

		Map<Long, ChatbotShoppingProduct> candidateMap = aiJudgeCandidates.stream()
			.filter(product -> product != null && product.productId() != null && product.isRecommendable())
			.collect(Collectors.toMap(
				ChatbotShoppingProduct::productId,
				product -> product,
				(left, right) -> left,
				LinkedHashMap::new
			));

		if (candidateMap.isEmpty()) {
			return List.of();
		}

		// 중요:
		// 여기서 batch loop 돌리면 서버 죽음.
		// 한 재료당 AI 호출 1번만 한다.
		List<IngredientProductJudgeItem> judgedItems = ingredientProductJudgeClient.judge(
			recipeName,
			ingredientName,
			new ArrayList<>(candidateMap.values()),
			PRODUCT_LIMIT_PER_INGREDIENT
		);

		if (judgedItems == null || judgedItems.isEmpty()) {
			return List.of();
		}

		return judgedItems.stream()
			.filter(item -> item != null && item.productId() != null)
			.filter(IngredientProductJudgeItem::isSuitable)
			.filter(item -> item.safeConfidence() >= MIN_JUDGE_CONFIDENCE)
			.filter(item -> candidateMap.containsKey(item.productId()))
			.sorted(
				Comparator.comparing(IngredientProductJudgeItem::safeConfidence)
					.reversed()
			)
			.map(item -> candidateMap.get(item.productId()))
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

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}