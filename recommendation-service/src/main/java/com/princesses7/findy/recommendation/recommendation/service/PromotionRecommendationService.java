package com.princesses7.findy.recommendation.recommendation.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.inventory.repository.InventorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.service.UserPreferenceQueryService;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionStatus;
import com.princesses7.findy.recommendation.promotion.repository.PromotionProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.support.RecommendationResultPolicy;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionRecommendationService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;

	private final PromotionProductSnapshotRepository promotionProductRepository;
	private final ProductSnapshotRepository productRepository;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final InventorySnapshotRepository inventoryRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final UserPreferenceQueryService userPreferenceQueryService;
	private final ProductEmbeddingClient productEmbeddingClient;
	private final PromotionRecommendationIntentBuilder intentBuilder;
	private final PromotionRecommendationScoreCalculator scoreCalculator;
	private final RecommendationRequestValidator requestValidator;
	private final PromotionRecommendationReasonBuilder reasonBuilder;

	public PromotionRecommendationResponse getPromotionRecommendations(
		Long userId,
		Long storeId,
		int size
	) {
		return getPromotionRecommendations(userId, storeId, size, null);
	}

	public PromotionRecommendationResponse getPromotionRecommendations(
		Long userId,
		Long storeId,
		int size,
		Long currentGridId
	) {
		validateRequest(userId, storeId);

		int normalizedSize = normalizeSize(size);
		UserPreferenceResponse userPreference = userPreferenceQueryService.getUserPreference(userId);

		List<PromotionProductSnapshot> activePromotionProducts = promotionProductRepository
			.findActivePromotionProducts(
				PromotionStatus.ENDED,
				LocalDateTime.now()
			);

		if (activePromotionProducts.isEmpty()) {
			return emptyResponse(userId, storeId, userPreference);
		}

		List<Long> productIds = activePromotionProducts.stream()
			.map(PromotionProductSnapshot::getProductId)
			.distinct()
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(productIds);
		Map<Long, ProductEmbedding> embeddingMap = findProductEmbeddingMap(productIds);
		Map<Long, InventorySnapshot> inventoryMap = findAvailableInventoryMap(storeId, productIds);
		Map<Long, String> categoryNameMap = findCategoryNameMap(
			productMap.values().stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		if (productMap.isEmpty() || inventoryMap.isEmpty()) {
			return emptyResponse(userId, storeId, userPreference);
		}

		List<Double> promotionIntentEmbedding = createPromotionIntentEmbedding(userPreference);

		if (embeddingMap.isEmpty() || promotionIntentEmbedding.isEmpty()) {
			return fallbackResponse(
				userId,
				storeId,
				userPreference,
				activePromotionProducts,
				productMap,
				inventoryMap,
				categoryNameMap,
				normalizedSize,
				currentGridId
			);
		}

		List<PromotionProductRecommendationResponse> recommendations = RecommendationResultPolicy
			.finalizePromotionRecommendations(
				activePromotionProducts.stream()
					.map(promotionProduct -> toRecommendation(
						promotionProduct,
						productMap,
						embeddingMap,
						inventoryMap,
						categoryNameMap,
						promotionIntentEmbedding,
						currentGridId
					))
					.filter(Objects::nonNull)
					.toList(),
				normalizedSize
			);

		if (recommendations.isEmpty()) {
			return fallbackResponse(
				userId,
				storeId,
				userPreference,
				activePromotionProducts,
				productMap,
				inventoryMap,
				categoryNameMap,
				normalizedSize,
				currentGridId
			);
		}

		return new PromotionRecommendationResponse(
			userId,
			storeId,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			recommendations
		);
	}

	private PromotionProductRecommendationResponse toRecommendation(
		PromotionProductSnapshot promotionProduct,
		Map<Long, ProductSnapshot> productMap,
		Map<Long, ProductEmbedding> embeddingMap,
		Map<Long, InventorySnapshot> inventoryMap,
		Map<Long, String> categoryNameMap,
		List<Double> promotionIntentEmbedding,
		Long currentGridId
	) {
		Long productId = promotionProduct.getProductId();

		ProductSnapshot product = productMap.get(productId);
		ProductEmbedding productEmbedding = embeddingMap.get(productId);
		InventorySnapshot inventory = inventoryMap.get(productId);

		if (product == null || productEmbedding == null || inventory == null) {
			return null;
		}

		double similarityScore = VectorSimilarityCalculator.cosineSimilarity(
			promotionIntentEmbedding,
			productEmbedding.getEmbeddingVector()
		);

		PromotionRecommendationScoreResult scoreResult = scoreCalculator.calculate(
			similarityScore,
			product,
			promotionProduct,
			inventory,
			currentGridId
		);

		String categoryName = categoryNameMap.getOrDefault(product.getCategoryId(), "");
		String reason = reasonBuilder.createAiReason(promotionProduct, scoreResult);

		return PromotionProductRecommendationResponse.of(
			product,
			categoryName,
			promotionProduct,
			inventory,
			scoreResult.totalScore(),
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			reason
		);
	}

	private List<Double> createPromotionIntentEmbedding(UserPreferenceResponse userPreference) {
		try {
			return productEmbeddingClient.createEmbedding(
				intentBuilder.build(userPreference)
			);
		} catch (BaseException exception) {
			return List.of();
		}
	}

	private PromotionRecommendationResponse fallbackResponse(
		Long userId,
		Long storeId,
		UserPreferenceResponse userPreference,
		List<PromotionProductSnapshot> activePromotionProducts,
		Map<Long, ProductSnapshot> productMap,
		Map<Long, InventorySnapshot> inventoryMap,
		Map<Long, String> categoryNameMap,
		int size,
		Long currentGridId
	) {
		List<PromotionProductRecommendationResponse> recommendations = RecommendationResultPolicy
			.finalizePromotionRecommendations(
				activePromotionProducts.stream()
					.map(promotionProduct -> toFallbackRecommendation(
						promotionProduct,
						productMap,
						inventoryMap,
						categoryNameMap,
						currentGridId
					))
					.filter(Objects::nonNull)
					.toList(),
				size
			);

		return new PromotionRecommendationResponse(
			userId,
			storeId,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			recommendations
		);
	}

	private PromotionProductRecommendationResponse toFallbackRecommendation(
		PromotionProductSnapshot promotionProduct,
		Map<Long, ProductSnapshot> productMap,
		Map<Long, InventorySnapshot> inventoryMap,
		Map<Long, String> categoryNameMap,
		Long currentGridId
	) {
		Long productId = promotionProduct.getProductId();

		ProductSnapshot product = productMap.get(productId);
		InventorySnapshot inventory = inventoryMap.get(productId);

		if (product == null || inventory == null) {
			return null;
		}

		PromotionRecommendationScoreResult scoreResult = scoreCalculator.calculateFallback(
			product,
			promotionProduct,
			inventory,
			currentGridId
		);

		String categoryName = categoryNameMap.getOrDefault(product.getCategoryId(), "");
		String reason = reasonBuilder.createFallbackReason(product, promotionProduct, scoreResult);

		return PromotionProductRecommendationResponse.of(
			product,
			categoryName,
			promotionProduct,
			inventory,
			scoreResult.totalScore(),
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			reason
		);
	}

	private Map<Long, ProductSnapshot> findRecommendableProductMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return productRepository.findByProductIdIn(productIds)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.collect(Collectors.toMap(
				ProductSnapshot::getProductId,
				product -> product,
				(left, right) -> left
			));
	}

	private Map<Long, ProductEmbedding> findProductEmbeddingMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return productEmbeddingRepository.findByProductIdIn(productIds)
			.stream()
			.filter(productEmbedding -> productEmbeddingClient.model().equals(productEmbedding.getModel()))
			.filter(productEmbedding -> productEmbeddingClient.dimensions() == productEmbedding.getDimensions())
			.collect(Collectors.toMap(
				ProductEmbedding::getProductId,
				productEmbedding -> productEmbedding,
				(left, right) -> left
			));
	}

	private Map<Long, InventorySnapshot> findAvailableInventoryMap(
		Long storeId,
		Collection<Long> productIds
	) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return inventoryRepository.findByStoreIdAndProductIdIn(storeId, productIds)
			.stream()
			.filter(InventorySnapshot::hasAvailableStock)
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

	private PromotionRecommendationResponse emptyResponse(
		Long userId,
		Long storeId,
		UserPreferenceResponse userPreference
	) {
		return new PromotionRecommendationResponse(
			userId,
			storeId,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			RecommendationType.AI_PERSONALIZED_PROMOTION,
			List.of()
		);
	}

	private void validateRequest(
		Long userId,
		Long storeId
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(storeId, "storeId");
	}

	private int normalizeSize(int size) {
		return requestValidator.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
	}
}