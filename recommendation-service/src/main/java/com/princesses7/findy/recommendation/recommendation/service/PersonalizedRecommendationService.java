package com.princesses7.findy.recommendation.recommendation.service;

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
import com.princesses7.findy.recommendation.preference.service.UserPreferenceQueryService;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.PersonalizedRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PersonalizedRecommendationService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;

	private final UserPreferenceQueryService userPreferenceQueryService;
	private final OpenAiEmbeddingClient openAiEmbeddingClient;
	private final OpenAiProperties openAiProperties;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductSnapshotRepository productRepository;

	@Transactional(readOnly = true)
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
				"NO_PREFERENCE_FALLBACK"
			);
		}

		List<Double> userPreferenceEmbedding = openAiEmbeddingClient.createEmbedding(
			userPreference.preferenceText()
		);

		List<ProductEmbedding> candidateEmbeddings = productEmbeddingRepository.findByModelAndDimensions(
			openAiProperties.embeddingModel(),
			openAiProperties.embeddingDimensions()
		);

		if (candidateEmbeddings.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				"NO_PRODUCT_EMBEDDING_FALLBACK"
			);
		}

		List<Long> productIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = productRepository.findByProductIdIn(productIds)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.collect(Collectors.toMap(
				ProductSnapshot::getProductId,
				product -> product
			));

		List<ProductRecommendationResponse> recommendations = candidateEmbeddings.stream()
			.map(productEmbedding -> {
				ProductSnapshot product = productMap.get(productEmbedding.getProductId());

				if (product == null) {
					return null;
				}

				double score = VectorSimilarityCalculator.cosineSimilarity(
					userPreferenceEmbedding,
					productEmbedding.getEmbeddingVector()
				);

				return ProductRecommendationResponse.from(
					product,
					score,
					"첫 로그인 설문에서 선택한 선호 카테고리와 쇼핑 스타일을 기반으로 추천한 상품입니다."
				);
			})
			.filter(response -> response != null)
			.sorted((left, right) -> Double.compare(right.score(), left.score()))
			.limit(normalizedSize)
			.toList();

		if (recommendations.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				"EMPTY_RECOMMENDATION_FALLBACK"
			);
		}

		return new PersonalizedRecommendationResponse(
			userId,
			"PREFERENCE_EMBEDDING",
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private PersonalizedRecommendationResponse fallback(
		Long userId,
		int size,
		UserPreferenceResponse userPreference,
		String baseType
	) {
		List<ProductRecommendationResponse> recommendations = productRepository.findByDeletedFalse(
				PageRequest.of(0, size)
			)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.map(product -> ProductRecommendationResponse.from(
				product,
				0.0,
				"추천 데이터가 부족하여 기본 상품을 제공합니다."
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

	private int normalizeSize(int size) {
		if (size <= 0) {
			return DEFAULT_SIZE;
		}

		return Math.min(size, MAX_SIZE);
	}
}