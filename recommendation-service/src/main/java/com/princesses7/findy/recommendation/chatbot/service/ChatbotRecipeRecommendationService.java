package com.princesses7.findy.recommendation.chatbot.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.chatbot.dto.ChatbotIntentAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientAnalysis;
import com.princesses7.findy.recommendation.chatbot.dto.RecipeIngredientItem;
import com.princesses7.findy.recommendation.chatbot.dto.request.ChatbotMessageRequest;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeIngredientRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeProductRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotRecipeRecommendationResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;
import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.inventory.repository.InventorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.support.RecommendationResultPolicy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotRecipeRecommendationService {

	private static final long DEFAULT_STORE_ID = 1L;
	private static final int PRODUCT_LIMIT_PER_INGREDIENT = 5;

	private final ChatbotRecipeIngredientExtractor recipeIngredientExtractor;
	private final ProductSnapshotRepository productRepository;
	private final InventorySnapshotRepository inventoryRepository;
	private final CategorySnapshotRepository categoryRepository;

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
		List<ProductSnapshot> products = findProductsByIngredient(ingredient.ingredientName());

		Map<Long, InventorySnapshot> inventoryMap = findInventoryMap(
			storeId,
			products.stream()
				.map(ProductSnapshot::getProductId)
				.toList()
		);

		Map<Long, String> categoryNameMap = findCategoryNameMap(
			products.stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		List<ChatbotRecipeProductRecommendationResponse> recommendedProducts = products.stream()
			.map(product -> ChatbotRecipeProductRecommendationResponse.from(
				userId,
				storeId,
				product,
				categoryNameMap.getOrDefault(product.getCategoryId(), ""),
				inventoryMap.get(product.getProductId()),
				isFirstProduct(products, product)
			))
			.toList();

		return new ChatbotRecipeIngredientRecommendationResponse(
			ingredient.ingredientName(),
			ingredient.quantityText(),
			recommendedProducts
		);
	}

	private List<ProductSnapshot> findProductsByIngredient(String ingredientName) {
		if (ingredientName == null || ingredientName.isBlank()) {
			return List.of();
		}

		List<ProductSnapshot> products = productRepository.searchByKeyword(
			ingredientName,
			PageRequest.of(0, PRODUCT_LIMIT_PER_INGREDIENT)
		);

		Map<Long, ProductSnapshot> productMap = new LinkedHashMap<>();

		for (ProductSnapshot product : products) {
			if (RecommendationResultPolicy.isDisplayableProduct(product)) {
				productMap.putIfAbsent(product.getProductId(), product);
			}
		}

		return productMap.values()
			.stream()
			.limit(PRODUCT_LIMIT_PER_INGREDIENT)
			.toList();
	}

	private Map<Long, InventorySnapshot> findInventoryMap(
		Long storeId,
		Collection<Long> productIds
	) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return inventoryRepository.findByStoreIdAndProductIdIn(storeId, productIds)
			.stream()
			.collect(Collectors.toMap(
				InventorySnapshot::getProductId,
				inventory -> inventory,
				(left, right) -> left
			));
	}

	private Map<Long, String> findCategoryNameMap(Collection<Long> categoryIds) {
		if (categoryIds.isEmpty()) {
			return Map.of();
		}

		return categoryRepository.findByCategoryIdIn(categoryIds)
			.stream()
			.filter(CategorySnapshot::isActive)
			.collect(Collectors.toMap(
				CategorySnapshot::getCategoryId,
				CategorySnapshot::getCategoryName,
				(left, right) -> left
			));
	}

	private boolean isFirstProduct(
		List<ProductSnapshot> products,
		ProductSnapshot product
	) {
		return !products.isEmpty()
			&& products.get(0).getProductId().equals(product.getProductId());
	}

	private Long normalizeStoreId(Long storeId) {
		if (storeId == null || storeId <= 0) {
			return DEFAULT_STORE_ID;
		}

		return storeId;
	}
}