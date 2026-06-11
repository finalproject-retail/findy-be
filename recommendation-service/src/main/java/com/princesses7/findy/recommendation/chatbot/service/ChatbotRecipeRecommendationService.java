package com.princesses7.findy.recommendation.chatbot.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
	private static final int PRODUCT_LIMIT_PER_INGREDIENT = 5;
	private static final int CANDIDATE_LIMIT_PER_INGREDIENT = 20;
	private static final double MIN_JUDGE_CONFIDENCE = 0.65;

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

		List<ChatbotRecipeIngredientRecommendationResponse> ingredients = recipeIngredientAnalysis.ingredients()
			.stream()
			.map(ingredient -> recommendIngredient(
				userId,
				storeId,
				recipeIngredientAnalysis.recipeName(),
				ingredient
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
		RecipeIngredientItem ingredient
	) {
		List<ChatbotShoppingProduct> products = findProductsByIngredient(
			recipeName,
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
		String recipeName,
		Long storeId,
		String ingredientName
	) {
		if (ingredientName == null || ingredientName.isBlank()) {
			return List.of();
		}

		List<ChatbotShoppingProduct> candidates = distinctRecommendableProducts(
			shoppingProductReadRepository.searchIngredientCandidates(
				ingredientName,
				storeId,
				CANDIDATE_LIMIT_PER_INGREDIENT
			),
			CANDIDATE_LIMIT_PER_INGREDIENT
		);

		if (candidates.isEmpty()) {
			return List.of();
		}

		List<IngredientProductJudgeItem> judgedItems = ingredientProductJudgeClient.judge(
			recipeName,
			ingredientName,
			candidates,
			PRODUCT_LIMIT_PER_INGREDIENT
		);

		if (judgedItems.isEmpty()) {
			return fallbackProducts(candidates);
		}

		Map<Long, IngredientProductJudgeItem> suitableJudgeMap = judgedItems.stream()
			.filter(IngredientProductJudgeItem::isSuitable)
			.filter(item -> item.safeConfidence() >= MIN_JUDGE_CONFIDENCE)
			.collect(Collectors.toMap(
				IngredientProductJudgeItem::productId,
				item -> item,
				(left, right) -> left,
				LinkedHashMap::new
			));

		if (suitableJudgeMap.isEmpty()) {
			return fallbackProducts(candidates);
		}

		Set<Long> candidateProductIds = candidates.stream()
			.map(ChatbotShoppingProduct::productId)
			.collect(Collectors.toSet());

		return suitableJudgeMap.values()
			.stream()
			.filter(item -> candidateProductIds.contains(item.productId()))
			.sorted(
				Comparator.comparing(IngredientProductJudgeItem::safeConfidence)
					.reversed()
			)
			.map(item -> findCandidate(candidates, item.productId()))
			.filter(product -> product != null)
			.limit(PRODUCT_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private ChatbotShoppingProduct findCandidate(
		List<ChatbotShoppingProduct> candidates,
		Long productId
	) {
		if (productId == null) {
			return null;
		}

		return candidates.stream()
			.filter(candidate -> productId.equals(candidate.productId()))
			.findFirst()
			.orElse(null);
	}

	private List<ChatbotShoppingProduct> fallbackProducts(List<ChatbotShoppingProduct> candidates) {
		return candidates.stream()
			.limit(PRODUCT_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private List<ChatbotShoppingProduct> distinctRecommendableProducts(
		List<ChatbotShoppingProduct> products,
		int limit
	) {
		Map<Long, ChatbotShoppingProduct> productMap = new LinkedHashMap<>();

		for (ChatbotShoppingProduct product : products) {
			if (product != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.productId(), product);
			}
		}

		return productMap.values()
			.stream()
			.limit(limit)
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