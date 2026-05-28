package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.princesses7.findy.recommendation.recommendation.log.repository.RecommendationLogRepository;
import com.princesses7.findy.recommendation.recommendation.log.repository.projection.PopularProductProjection;
import com.princesses7.findy.recommendation.recommendation.support.RecommendationResultPolicy;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationBaseType;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedRecommendationService {

	private static final int DEFAULT_SIZE = 10;
	private static final int MAX_SIZE = 30;
	private static final int FALLBACK_MULTIPLIER = 3;
	private static final int POPULAR_LOOKBACK_DAYS = 14;

	private final UserPreferenceQueryService userPreferenceQueryService;
	private final OpenAiEmbeddingClient openAiEmbeddingClient;
	private final OpenAiProperties openAiProperties;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final PersonalizedRecommendationScorer scorer;
	private final RecommendationRequestValidator requestValidator;
	private final RecommendationLogRepository recommendationLogRepository;

	public PersonalizedRecommendationResponse getPersonalizedRecommendations(
		Long userId,
		int size
	) {
		requestValidator.validatePositiveId(userId, "userId");
		int normalizedSize = normalizeSize(size);

		UserPreferenceResponse userPreference = userPreferenceQueryService.getUserPreference(userId);
		RecommendationBaseType baseType = resolveBaseType(userPreference);

		if (baseType == RecommendationBaseType.POPULAR_FALLBACK) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				RecommendationBaseType.POPULAR_FALLBACK
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

		List<ProductRecommendationResponse> recommendations = RecommendationResultPolicy.finalizeProductRecommendations(
			candidateEmbeddings.stream()
				.map(productEmbedding -> toRecommendationResponse(
					userPreference,
					productEmbedding,
					productMap,
					categoryNameMap,
					userPreferenceEmbedding
				))
				.filter(Objects::nonNull)
				.toList(),
			normalizedSize
		);

		if (recommendations.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				RecommendationBaseType.POPULAR_FALLBACK
			);
		}

		return new PersonalizedRecommendationResponse(
			userId,
			baseType,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private RecommendationBaseType resolveBaseType(UserPreferenceResponse userPreference) {
		boolean hasPreference = userPreferenceQueryService.hasPreference(userPreference);

		/*
		 * 현재 작업 범위는 선호 정보 기반 개인 맞춤 추천입니다.
		 * 구매 기록 조회 로직은 아직 연결하지 않았기 때문에 false로 둡니다.
		 *
		 * 이후 구매 기록 기능을 붙이면
		 * hasPurchaseHistory 값을 실제 구매 기록 존재 여부로 교체하면 됩니다.
		 */
		// TODO: 구매 기록 조회 로직과 연결
		boolean hasPurchaseHistory = false;

		if (hasPreference && hasPurchaseHistory) {
			return RecommendationBaseType.PREFERENCE_WITH_PURCHASE_HISTORY;
		}

		if (hasPreference) {
			return RecommendationBaseType.PREFERENCE_ONLY;
		}

		if (hasPurchaseHistory) {
			return RecommendationBaseType.PURCHASE_HISTORY_ONLY;
		}

		return RecommendationBaseType.POPULAR_FALLBACK;
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
		List<ProductRecommendationResponse> recommendations = List.of();

		if (baseType == RecommendationBaseType.POPULAR_FALLBACK) {
			recommendations = findPopularFallbackRecommendations(size);
		}

		if (recommendations.isEmpty()) {
			recommendations = findDefaultFallbackRecommendations(size, baseType);
		}

		return new PersonalizedRecommendationResponse(
			userId,
			baseType,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private List<ProductRecommendationResponse> findPopularFallbackRecommendations(int size) {
		LocalDateTime fromDateTime = LocalDateTime.now().minusDays(POPULAR_LOOKBACK_DAYS);

		List<PopularProductProjection> popularProducts = recommendationLogRepository.findPopularPersonalizedProducts(
			fromDateTime,
			size * FALLBACK_MULTIPLIER
		);

		if (popularProducts.isEmpty()) {
			return List.of();
		}

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(
			popularProducts.stream()
				.map(PopularProductProjection::getProductId)
				.toList()
		);

		double maxPopularityScore = popularProducts.stream()
			.mapToLong(popularProduct -> defaultLong(popularProduct.getPopularityScore()))
			.max()
			.orElse(1L);

		return RecommendationResultPolicy.finalizeProductRecommendations(
			popularProducts.stream()
				.map(popularProduct -> toPopularFallbackResponse(
					popularProduct,
					productMap,
					maxPopularityScore
				))
				.filter(Objects::nonNull)
				.toList(),
			size
		);
	}

	private ProductRecommendationResponse toPopularFallbackResponse(
		PopularProductProjection popularProduct,
		Map<Long, ProductSnapshot> productMap,
		double maxPopularityScore
	) {
		ProductSnapshot product = productMap.get(popularProduct.getProductId());

		if (product == null) {
			return null;
		}

		return ProductRecommendationResponse.from(
			product,
			scorer.popularFallbackScore(
				product,
				defaultLong(popularProduct.getPopularityScore()),
				maxPopularityScore
			),
			RecommendationType.PERSONALIZED,
			createPopularFallbackReason(popularProduct)
		);
	}

	private List<ProductRecommendationResponse> findDefaultFallbackRecommendations(
		int size,
		RecommendationBaseType baseType
	) {
		return RecommendationResultPolicy.finalizeProductRecommendations(
			productRepository.findByDeletedFalse(PageRequest.of(0, size * FALLBACK_MULTIPLIER))
				.stream()
				.filter(RecommendationResultPolicy::isDisplayableProduct)
				.map(product -> ProductRecommendationResponse.from(
					product,
					scorer.fallbackScore(product),
					RecommendationType.PERSONALIZED,
					createFallbackReason(baseType)
				))
				.toList(),
			size
		);
	}

	private String createPopularFallbackReason(PopularProductProjection popularProduct) {
		if (defaultLong(popularProduct.getClickCount()) > 0) {
			return "개인화 데이터가 부족하여 최근 클릭 반응이 높은 인기 상품을 추천합니다.";
		}

		return "개인화 데이터가 부족하여 최근 추천 화면에 자주 노출된 인기 상품을 추천합니다.";
	}

	private long defaultLong(Long value) {
		return value == null ? 0L : value;
	}

	private String createFallbackReason(RecommendationBaseType baseType) {
		if (baseType == RecommendationBaseType.NO_PRODUCT_EMBEDDING_FALLBACK) {
			return "상품 임베딩 데이터가 부족하여 구매 가능한 상품을 기준으로 추천합니다.";
		}

		return "개인화 데이터가 부족하여 구매 가능한 상품을 기준으로 추천합니다.";
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
		return requestValidator.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
	}
}