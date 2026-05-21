package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.external.openai.OpenAiEmbeddingClient;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.preference.service.UserPreferenceQueryService;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.PersonalizedRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationBaseType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedRecommendationService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;
	private static final int FALLBACK_MULTIPLIER = 3;

	private final UserPreferenceQueryService userPreferenceQueryService;
	private final OpenAiEmbeddingClient openAiEmbeddingClient;
	private final OpenAiProperties openAiProperties;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final PersonalizedRecommendationScorer scorer;

	public PersonalizedRecommendationResponse getPersonalizedRecommendations(
		Long userId,
		int size
	) {
		int normalizedSize = normalizeSize(size);

		UserPreferenceResponse userPreference = userPreferenceQueryService.getUserPreference(userId);

		if (!userPreferenceQueryService.hasPreference(userPreference)) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				RecommendationBaseType.NO_PREFERENCE_FALLBACK
			);
		}

		List<ProductEmbedding> candidateEmbeddings = productEmbeddingRepository.findByModelAndDimensions(
			openAiProperties.embeddingModel(),
			openAiProperties.embeddingDimensions()
		);

		if (candidateEmbeddings.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				RecommendationBaseType.NO_PRODUCT_EMBEDDING_FALLBACK
			);
		}

		List<Double> userPreferenceEmbedding = openAiEmbeddingClient.createEmbedding(
			userPreference.preferenceText()
		);

		List<Long> productIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(productIds);

		Map<Long, String> categoryNameMap = findCategoryNameMap(
			productMap.values().stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		List<ProductRecommendationResponse> recommendations = candidateEmbeddings.stream()
			.map(productEmbedding -> toRecommendationResponse(
				userPreference,
				productEmbedding,
				productMap,
				categoryNameMap,
				userPreferenceEmbedding
			))
			.filter(response -> response != null)
			.sorted(
				Comparator.comparing(ProductRecommendationResponse::score)
					.reversed()
					.thenComparing(ProductRecommendationResponse::productId)
			)
			.limit(normalizedSize)
			.toList();

		if (recommendations.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				RecommendationBaseType.EMPTY_RECOMMENDATION_FALLBACK
			);
		}

		return new PersonalizedRecommendationResponse(
			userId,
			RecommendationBaseType.PREFERENCE_EMBEDDING,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private ProductRecommendationResponse toRecommendationResponse(
		UserPreferenceResponse userPreference,
		ProductEmbedding productEmbedding,
		Map<Long, ProductSnapshot> productMap,
		Map<Long, String> categoryNameMap,
		List<Double> userPreferenceEmbedding
	) {
		ProductSnapshot product = productMap.get(productEmbedding.getProductId());

		if (product == null) {
			return null;
		}

		String categoryName = categoryNameMap.getOrDefault(product.getCategoryId(), "");

		double similarityScore = VectorSimilarityCalculator.cosineSimilarity(
			userPreferenceEmbedding,
			productEmbedding.getEmbeddingVector()
		);

		double score = scorer.calculate(
			userPreference,
			product,
			categoryName,
			similarityScore
		);

		return ProductRecommendationResponse.from(
			product,
			score,
			RecommendationType.PERSONALIZED,
			scorer.createReason(userPreference, product, categoryName)
		);
	}

	private PersonalizedRecommendationResponse fallback(
		Long userId,
		int size,
		UserPreferenceResponse userPreference,
		RecommendationBaseType baseType
	) {
		List<ProductRecommendationResponse> recommendations = productRepository.findByDeletedFalse(
				PageRequest.of(0, size * FALLBACK_MULTIPLIER)
			)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.sorted(
				Comparator.comparing(this::getDiscountRate)
					.reversed()
					.thenComparing(ProductSnapshot::getProductId)
			)
			.limit(size)
			.map(product -> ProductRecommendationResponse.from(
				product,
				scorer.fallbackScore(product),
				RecommendationType.PERSONALIZED,
				"추천 데이터가 부족하여 구매 가능한 상품을 기준으로 추천합니다."
			))
			.toList();

		return new PersonalizedRecommendationResponse(
			userId,
			baseType,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private Map<Long, ProductSnapshot> findRecommendableProductMap(List<Long> productIds) {
		return productRepository.findByProductIdIn(productIds)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.collect(Collectors.toMap(
				ProductSnapshot::getProductId,
				product -> product,
				(left, right) -> left
			));
	}

	private Map<Long, String> findCategoryNameMap(Collection<Long> categoryIds) {
		if (categoryIds.isEmpty()) {
			return Map.of();
		}

		return categoryRepository.findByCategoryIdIn(categoryIds)
			.stream()
			.collect(Collectors.toMap(
				CategorySnapshot::getCategoryId,
				CategorySnapshot::getCategoryName,
				(left, right) -> left
			));
	}

	private BigDecimal getDiscountRate(ProductSnapshot product) {
		if (product.getDiscountRate() == null) {
			return BigDecimal.ZERO;
		}

		return product.getDiscountRate();
	}

	private int normalizeSize(int size) {
		if (size <= 0) {
			return DEFAULT_SIZE;
		}

		return Math.min(size, MAX_SIZE);
	}
}