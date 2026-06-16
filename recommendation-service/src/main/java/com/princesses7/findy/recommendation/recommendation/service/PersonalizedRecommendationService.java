package com.princesses7.findy.recommendation.recommendation.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
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
import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;
import com.princesses7.findy.recommendation.external.shopping.PurchaseHistoryClient;
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
	private static final int MIN_CANDIDATE_SIZE = 500;
	private static final int CANDIDATE_MULTIPLIER = 100;
	private static final int MAX_CANDIDATE_SIZE = 2_000;

	private final UserPreferenceQueryService userPreferenceQueryService;
	private final ProductEmbeddingClient productEmbeddingClient;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final PersonalizedRecommendationScorer scorer;
	private final RecommendationRequestValidator requestValidator;
	private final RecommendationLogRepository recommendationLogRepository;
	private final PurchaseHistoryClient purchaseHistoryClient;
	private final InventorySnapshotRepository inventoryRepository;
	private final PromotionProductSnapshotRepository promotionProductRepository;

	public PersonalizedRecommendationResponse getPersonalizedRecommendations(
		Long userId,
		Long storeId,
		int size
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(storeId, "storeId");
		int normalizedSize = normalizeSize(size);

		UserPreferenceResponse userPreference = userPreferenceQueryService.getUserPreference(userId);
		PurchaseHistoryContext purchaseHistory = PurchaseHistoryContext.from(
			purchaseHistoryClient.findFrequentPurchaseProducts(userId)
		);
		RecommendationBaseType baseType = resolveBaseType(userPreference, purchaseHistory);

		if (baseType == RecommendationBaseType.POPULAR_FALLBACK) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				purchaseHistory,
				storeId,
				RecommendationBaseType.POPULAR_FALLBACK
			);
		}

		List<ProductEmbedding> candidateEmbeddings = productEmbeddingRepository.findRecommendableCandidates(
			productEmbeddingClient.model(),
			productEmbeddingClient.dimensions(),
			storeId,
			PageRequest.of(0, calculateCandidateSize(normalizedSize)));

		if (candidateEmbeddings.isEmpty()) {
			return fallback(
				userId,
				normalizedSize,
				userPreference,
				purchaseHistory,
				storeId,
				RecommendationBaseType.NO_PRODUCT_EMBEDDING_FALLBACK
			);
		}

		List<Double> userPreferenceEmbedding = productEmbeddingClient.createEmbedding(
			createPersonalizationText(userPreference, purchaseHistory)
		);

		List<Long> productIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(productIds, storeId);
		Map<Long, PromotionProductSnapshot> promotionProductMap = findActivePromotionProductMap(productMap.keySet());

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
					promotionProductMap,
					categoryNameMap,
					userPreferenceEmbedding,
					purchaseHistory
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
				purchaseHistory,
				storeId,
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

	private RecommendationBaseType resolveBaseType(
		UserPreferenceResponse userPreference,
		PurchaseHistoryContext purchaseHistory
	) {
		boolean hasPreference = userPreferenceQueryService.hasPreference(userPreference);
		boolean hasPurchaseHistory = purchaseHistory.hasHistory();

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
		Map<Long, PromotionProductSnapshot> promotionProductMap,
		Map<Long, String> categoryNameMap,
		List<Double> userPreferenceEmbedding,
		PurchaseHistoryContext purchaseHistory
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
			similarityScore,
			purchaseHistory
		);

		return ProductRecommendationResponse.from(
			product,
			promotionProductMap.get(product.getProductId()),
			score,
			RecommendationType.PERSONALIZED,
			scorer.createReason(userPreference, product, categoryName, purchaseHistory)
		);
	}

	private PersonalizedRecommendationResponse fallback(
		Long userId,
		int size,
		UserPreferenceResponse userPreference,
		PurchaseHistoryContext purchaseHistory,
		Long storeId,
		RecommendationBaseType baseType
	) {
		List<ProductRecommendationResponse> recommendations = List.of();

		if (purchaseHistory.hasHistory()) {
			recommendations = findPurchaseHistoryRecommendations(size, purchaseHistory, storeId);
		}

		if (recommendations.isEmpty() && baseType == RecommendationBaseType.POPULAR_FALLBACK) {
			recommendations = findPopularFallbackRecommendations(size, storeId);
		}

		if (recommendations.isEmpty()) {
			recommendations = findDefaultFallbackRecommendations(size, storeId, baseType);
		}

		return new PersonalizedRecommendationResponse(
			userId,
			baseType,
			userPreference.preferredCategories(),
			userPreference.shoppingStyles(),
			recommendations
		);
	}

	private List<ProductRecommendationResponse> findPurchaseHistoryRecommendations(
		int size,
		PurchaseHistoryContext purchaseHistory,
		Long storeId
	) {
		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(
			purchaseHistory.purchasedProductIds()
				.stream()
				.toList(),
			storeId
		);

		if (productMap.isEmpty()) {
			return List.of();
		}

		Map<Long, PromotionProductSnapshot> promotionProductMap = findActivePromotionProductMap(productMap.keySet());

		return RecommendationResultPolicy.finalizeProductRecommendations(
			productMap.values()
				.stream()
				.map(product -> ProductRecommendationResponse.from(
					product,
					promotionProductMap.get(product.getProductId()),
					Math.min(0.70 + purchaseHistory.productAffinity(product) * 0.30, 1.0),
					RecommendationType.PERSONALIZED,
					"최근 구매 이력에서 자주 구매한 상품을 기반으로 추천합니다."
				))
				.toList(),
			size
		);
	}

	private String createPersonalizationText(
		UserPreferenceResponse userPreference,
		PurchaseHistoryContext purchaseHistory
	) {
		return """
			%s
			
			%s
			""".formatted(
			userPreference.preferenceText(),
			purchaseHistory.toPreferenceText()
		);
	}

	private List<ProductRecommendationResponse> findPopularFallbackRecommendations(int size, Long storeId) {
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
				.toList(),
			storeId
		);

		Map<Long, PromotionProductSnapshot> promotionProductMap = findActivePromotionProductMap(productMap.keySet());

		double maxPopularityScore = popularProducts.stream()
			.mapToLong(popularProduct -> defaultLong(popularProduct.getPopularityScore()))
			.max()
			.orElse(1L);

		return RecommendationResultPolicy.finalizeProductRecommendations(
			popularProducts.stream()
				.map(popularProduct -> toPopularFallbackResponse(
					popularProduct,
					productMap,
					promotionProductMap,
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
		Map<Long, PromotionProductSnapshot> promotionProductMap,
		double maxPopularityScore
	) {
		ProductSnapshot product = productMap.get(popularProduct.getProductId());

		if (product == null) {
			return null;
		}

		return ProductRecommendationResponse.from(
			product,
			promotionProductMap.get(product.getProductId()),
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
		Long storeId,
		RecommendationBaseType baseType
	) {
		List<ProductSnapshot> products = productRepository
			.findByDeletedAtIsNull(PageRequest.of(0, size * FALLBACK_MULTIPLIER * FALLBACK_MULTIPLIER))
			.stream()
			.filter(RecommendationResultPolicy::isDisplayableProduct)
			.filter(product -> hasAvailableStock(product, storeId))
			.toList();

		Map<Long, PromotionProductSnapshot> promotionProductMap = findActivePromotionProductMap(
			products.stream()
				.map(ProductSnapshot::getProductId)
				.toList()
		);

		return RecommendationResultPolicy.finalizeProductRecommendations(
			products.stream()
				.map(product -> ProductRecommendationResponse.from(
					product,
					promotionProductMap.get(product.getProductId()),
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

	private Map<Long, ProductSnapshot> findRecommendableProductMap(List<Long> productIds, Long storeId) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		Map<Long, InventorySnapshot> availableInventoryMap = findAvailableInventoryMap(storeId, productIds);

		if (availableInventoryMap.isEmpty()) {
			return Map.of();
		}

		return productRepository.findByProductIdIn(productIds)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.filter(product -> availableInventoryMap.containsKey(product.getProductId()))
			.collect(Collectors.toMap(
				ProductSnapshot::getProductId,
				product -> product,
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

	private Map<Long, PromotionProductSnapshot> findActivePromotionProductMap(Collection<Long> productIds) {
		if (productIds.isEmpty()) {
			return Map.of();
		}

		return promotionProductRepository.findActivePromotionProductsByProductIds(
				productIds,
				PromotionStatus.ENDED,
				LocalDateTime.now()
			)
			.stream()
			.collect(Collectors.toMap(
				PromotionProductSnapshot::getProductId,
				promotionProduct -> promotionProduct,
				this::selectBetterPromotionProduct
			));
	}

	private PromotionProductSnapshot selectBetterPromotionProduct(
		PromotionProductSnapshot left,
		PromotionProductSnapshot right
	) {
		Integer leftPrice = left.getPromotionPrice();
		Integer rightPrice = right.getPromotionPrice();

		if (leftPrice == null && rightPrice == null) {
			return left;
		}

		if (leftPrice == null) {
			return right;
		}

		if (rightPrice == null) {
			return left;
		}

		return Comparator.<Integer>naturalOrder().compare(leftPrice, rightPrice) <= 0 ? left : right;
	}

	private boolean hasAvailableStock(ProductSnapshot product, Long storeId) {
		return inventoryRepository.findByProductIdAndStoreId(product.getProductId(), storeId)
			.map(InventorySnapshot::hasAvailableStock)
			.orElse(false);
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

	private int calculateCandidateSize(int size) {
		return Math.min(
			MAX_CANDIDATE_SIZE,
			Math.max(MIN_CANDIDATE_SIZE, size * CANDIDATE_MULTIPLIER));
	}

	private int normalizeSize(int size) {
		return requestValidator.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
	}
}