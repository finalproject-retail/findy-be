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
	private static final double MIN_JUDGE_CONFIDENCE = 0.5;

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
			ingredient
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
		RecipeIngredientItem ingredient
	) {
		if (ingredient == null || !hasText(ingredient.ingredientName())) {
			return List.of();
		}

		String ingredientName = ingredient.ingredientName();
		String searchKeyword = normalizeSearchKeyword(ingredient);

		List<ChatbotShoppingProduct> candidates = searchCandidates(
			ingredientName,
			searchKeyword,
			storeId
		);

		if (candidates.isEmpty()) {
			return List.of();
		}

		List<IngredientProductJudgeItem> judgedItems = ingredientProductJudgeClient.judge(
			recipeName,
			searchKeyword,
			candidates,
			PRODUCT_LIMIT_PER_INGREDIENT
		);

		if (judgedItems.isEmpty()) {
			return fallbackProducts(searchKeyword, candidates);
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
			return fallbackProducts(searchKeyword, candidates);
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

	private List<ChatbotShoppingProduct> searchCandidates(
		String ingredientName,
		String searchKeyword,
		Long storeId
	) {
		Map<Long, ChatbotShoppingProduct> productMap = new LinkedHashMap<>();

		addCandidates(
			productMap,
			shoppingProductReadRepository.searchIngredientCandidates(
				searchKeyword,
				storeId,
				CANDIDATE_LIMIT_PER_INGREDIENT
			)
		);

		if (!normalizeText(searchKeyword).equals(normalizeText(ingredientName))) {
			addCandidates(
				productMap,
				shoppingProductReadRepository.searchIngredientCandidates(
					ingredientName,
					storeId,
					CANDIDATE_LIMIT_PER_INGREDIENT
				)
			);
		}

		return productMap.values()
			.stream()
			.limit(CANDIDATE_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private void addCandidates(
		Map<Long, ChatbotShoppingProduct> productMap,
		List<ChatbotShoppingProduct> products
	) {
		if (products == null || products.isEmpty()) {
			return;
		}

		for (ChatbotShoppingProduct product : products) {
			if (product != null && product.productId() != null && product.isRecommendable()) {
				productMap.putIfAbsent(product.productId(), product);
			}
		}
	}

	private List<ChatbotShoppingProduct> fallbackProducts(
		String searchKeyword,
		List<ChatbotShoppingProduct> candidates
	) {
		return candidates.stream()
			.filter(product -> isStrictSearchKeywordProduct(searchKeyword, product))
			.limit(PRODUCT_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private boolean isStrictSearchKeywordProduct(
		String searchKeyword,
		ChatbotShoppingProduct product
	) {
		if (!hasText(searchKeyword) || product == null || !hasText(product.productName())) {
			return false;
		}

		String keyword = normalizeText(searchKeyword);
		String productName = normalizeText(product.productName());

		if (productName.equals(keyword)) {
			return true;
		}

		if (!productName.startsWith(keyword)) {
			return false;
		}

		if (productName.length() == keyword.length()) {
			return true;
		}

		char next = productName.charAt(keyword.length());

		return Character.isDigit(next)
			|| Character.isWhitespace(next)
			|| next == '-'
			|| next == '_'
			|| next == '/'
			|| next == '('
			|| next == '[';
	}

	private String normalizeSearchKeyword(RecipeIngredientItem ingredient) {
		if (hasText(ingredient.searchKeyword())) {
			return ingredient.searchKeyword().trim();
		}

		return ingredient.ingredientName().trim();
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

	private String normalizeText(String value) {
		if (value == null) {
			return "";
		}

		return value.trim()
			.replaceAll("\\s+", " ");
	}
}