package com.princesses7.findy.recommendation.recommendation.service;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.service.ProductEmbeddingService;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.external.embedding.ProductEmbeddingClient;
import com.princesses7.findy.recommendation.external.rerank.RelatedProductRerankClient;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationListResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.RelatedProductRerankItem;
import com.princesses7.findy.recommendation.recommendation.dto.response.SourceProductResponse;
import com.princesses7.findy.recommendation.recommendation.support.RecommendationResultPolicy;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatedRecommendationService {

	private static final int DEFAULT_SIZE = 5;
	private static final int MAX_SIZE = 20;
	private static final int VECTOR_CANDIDATE_LIMIT = 30;
	private static final int FALLBACK_SEARCH_MULTIPLIER = 3;

	private static final double MIN_VECTOR_SCORE = 0.35;
	private static final double MIN_RERANK_SCORE = 0.55;
	private static final double CATEGORY_FALLBACK_SCORE = 0.30;
	private static final double GENERAL_FALLBACK_SCORE = 0.20;

	private final ProductSnapshotRepository productRepository;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductEmbeddingService productEmbeddingService;
	private final CategorySnapshotRepository categoryRepository;
	private final ProductEmbeddingClient productEmbeddingClient;
	private final RelatedProductRerankClient rerankClient;
	private final RecommendationRequestValidator requestValidator;

	@Transactional
	public ProductRecommendationListResponse getRelatedRecommendations(
		Long userId,
		Long productId,
		int size
	) {
		requestValidator.validatePositiveId(userId, "userId");
		requestValidator.validatePositiveId(productId, "productId");
		int normalizedSize = normalizeSize(size);

		ProductSnapshot sourceProduct = productRepository.findById(productId)
			.orElseThrow(() -> new BaseException(ErrorCode.RECOMMENDATION_PRODUCT_NOT_FOUND));

		String sourceCategoryName = categoryRepository.findById(sourceProduct.getCategoryId())
			.map(CategorySnapshot::getCategoryName)
			.orElse("");

		ProductEmbedding sourceEmbedding = getOrCreateSourceEmbedding(sourceProduct);
		List<Double> relatedIntentEmbedding = createRelatedIntentEmbedding(sourceProduct, sourceCategoryName);

		if (sourceEmbedding == null || relatedIntentEmbedding.isEmpty()) {
			return fallbackResponse(
				userId,
				sourceProduct,
				sourceCategoryName,
				normalizedSize,
				"임베딩 생성이 어려워 카테고리 정보를 기준으로 추천한 상품입니다."
			);
		}

		List<ProductEmbedding> candidateEmbeddings = productEmbeddingRepository.findByModelAndDimensions(
			productEmbeddingClient.model(),
			productEmbeddingClient.dimensions()
		);

		if (candidateEmbeddings.isEmpty()) {
			return fallbackResponse(
				userId,
				sourceProduct,
				sourceCategoryName,
				normalizedSize,
				"임베딩 후보가 부족하여 카테고리 정보를 기준으로 추천한 상품입니다."
			);
		}

		List<Long> candidateProductIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(candidateProductIds);

		if (productMap.isEmpty()) {
			return fallbackResponse(
				userId,
				sourceProduct,
				sourceCategoryName,
				normalizedSize,
				"추천 가능한 임베딩 상품이 부족하여 카테고리 정보를 기준으로 추천한 상품입니다."
			);
		}

		Map<Long, String> categoryNameMap = findCategoryNameMap(
			productMap.values()
				.stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		List<RelatedCandidate> vectorCandidates = candidateEmbeddings.stream()
			.map(candidateEmbedding -> toRelatedCandidate(
				sourceProduct,
				sourceEmbedding,
				candidateEmbedding,
				productMap,
				relatedIntentEmbedding
			))
			.filter(Objects::nonNull)
			.filter(candidate -> candidate.vectorScore() >= MIN_VECTOR_SCORE)
			.sorted(
				Comparator.comparing(RelatedCandidate::vectorScore)
					.reversed()
					.thenComparing(candidate -> candidate.product().getProductId())
			)
			.limit(VECTOR_CANDIDATE_LIMIT)
			.toList();

		if (vectorCandidates.isEmpty()) {
			return fallbackResponse(
				userId,
				sourceProduct,
				sourceCategoryName,
				normalizedSize,
				"벡터 유사도 기준을 통과한 후보가 부족하여 카테고리 정보를 기준으로 추천한 상품입니다."
			);
		}

		List<ProductSnapshot> rerankTargets = vectorCandidates.stream()
			.map(RelatedCandidate::product)
			.toList();

		List<RelatedProductRerankItem> rerankItems = rerankSafely(
			sourceProduct,
			sourceCategoryName,
			rerankTargets,
			categoryNameMap,
			normalizedSize
		);

		List<ProductRecommendationResponse> recommendations = toRerankedRecommendations(
			vectorCandidates,
			rerankItems,
			normalizedSize
		);

		if (recommendations.isEmpty()) {
			recommendations = toVectorFallbackRecommendations(vectorCandidates, normalizedSize);
		}

		recommendations = RecommendationResultPolicy.finalizeProductRecommendations(
			recommendations,
			normalizedSize
		);

		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			RecommendationType.RELATED,
			recommendations
		);
	}

	private ProductEmbedding getOrCreateSourceEmbedding(ProductSnapshot sourceProduct) {
		try {
			return productEmbeddingService.getOrCreateProductEmbedding(sourceProduct);
		} catch (Exception exception) {
			log.warn(
				"Related recommendation source embedding create failed. productId={}, message={}",
				sourceProduct.getProductId(),
				exception.getMessage()
			);
			return null;
		}
	}

	private List<Double> createRelatedIntentEmbedding(
		ProductSnapshot sourceProduct,
		String sourceCategoryName
	) {
		try {
			return productEmbeddingClient.createEmbedding(
				sourceProduct.toRelatedRecommendationText(sourceCategoryName)
			);
		} catch (Exception exception) {
			log.warn(
				"Related recommendation intent embedding create failed. productId={}, message={}",
				sourceProduct.getProductId(),
				exception.getMessage()
			);
			return List.of();
		}
	}

	private List<RelatedProductRerankItem> rerankSafely(
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		List<ProductSnapshot> rerankTargets,
		Map<Long, String> categoryNameMap,
		int normalizedSize
	) {
		try {
			return rerankClient.rerank(
				sourceProduct,
				sourceCategoryName,
				rerankTargets,
				categoryNameMap,
				normalizedSize
			);
		} catch (Exception exception) {
			log.warn(
				"Related recommendation rerank failed. productId={}, targetCount={}, message={}",
				sourceProduct.getProductId(),
				rerankTargets.size(),
				exception.getMessage()
			);
			return List.of();
		}
	}

	private RelatedCandidate toRelatedCandidate(
		ProductSnapshot sourceProduct,
		ProductEmbedding sourceEmbedding,
		ProductEmbedding candidateEmbedding,
		Map<Long, ProductSnapshot> productMap,
		List<Double> relatedIntentEmbedding
	) {
		ProductSnapshot candidate = productMap.get(candidateEmbedding.getProductId());

		if (candidate == null) {
			return null;
		}

		if (candidate.getProductId().equals(sourceProduct.getProductId())) {
			return null;
		}

		double relatedSimilarity = VectorSimilarityCalculator.cosineSimilarity(
			relatedIntentEmbedding,
			candidateEmbedding.getEmbeddingVector()
		);

		double sourceSimilarity = VectorSimilarityCalculator.cosineSimilarity(
			sourceEmbedding.getEmbeddingVector(),
			candidateEmbedding.getEmbeddingVector()
		);

		double score = calculateVectorScore(
			sourceProduct,
			candidate,
			relatedSimilarity,
			sourceSimilarity
		);

		return new RelatedCandidate(candidate, score);
	}

	private double calculateVectorScore(
		ProductSnapshot sourceProduct,
		ProductSnapshot candidate,
		double relatedSimilarity,
		double sourceSimilarity
	) {
		double score = normalizeSimilarity(relatedSimilarity) * 0.75;

		if (candidate.getCategoryId().equals(sourceProduct.getCategoryId())) {
			score -= 0.20;
		} else {
			score += 0.04;
		}

		if (sourceSimilarity >= 0.85) {
			score -= 0.18;
		} else if (sourceSimilarity >= 0.78) {
			score -= 0.10;
		}

		return clamp(score);
	}

	private List<ProductRecommendationResponse> toRerankedRecommendations(
		List<RelatedCandidate> vectorCandidates,
		List<RelatedProductRerankItem> rerankItems,
		int size
	) {
		if (rerankItems == null || rerankItems.isEmpty()) {
			return List.of();
		}

		Map<Long, RelatedProductRerankItem> rerankMap = rerankItems.stream()
			.filter(RelatedProductRerankItem::isComplementary)
			.filter(item -> item.safeScore() >= MIN_RERANK_SCORE)
			.collect(Collectors.toMap(
				RelatedProductRerankItem::productId,
				item -> item,
				(left, right) -> left
			));

		return RecommendationResultPolicy.finalizeProductRecommendations(
			vectorCandidates.stream()
				.filter(candidate -> rerankMap.containsKey(candidate.product().getProductId()))
				.map(candidate -> {
					RelatedProductRerankItem item = rerankMap.get(candidate.product().getProductId());
					double finalScore = calculateFinalScore(candidate.vectorScore(), item.safeScore());

					return ProductRecommendationResponse.from(
						candidate.product(),
						finalScore,
						RecommendationType.RELATED,
						item.safeReason()
					);
				})
				.toList(),
			size
		);
	}

	private List<ProductRecommendationResponse> toVectorFallbackRecommendations(
		List<RelatedCandidate> vectorCandidates,
		int size
	) {
		return RecommendationResultPolicy.finalizeProductRecommendations(
			vectorCandidates.stream()
				.map(candidate -> ProductRecommendationResponse.from(
					candidate.product(),
					candidate.vectorScore(),
					RecommendationType.RELATED,
					"AI 재정렬 결과가 부족하여 벡터 유사도 기반으로 추천한 연관 상품입니다."
				))
				.toList(),
			size
		);
	}

	private ProductRecommendationListResponse fallbackResponse(
		Long userId,
		ProductSnapshot sourceProduct,
		String sourceCategoryName,
		int normalizedSize,
		String reason
	) {
		List<ProductRecommendationResponse> recommendations = findFallbackRecommendations(
			sourceProduct,
			normalizedSize,
			reason
		);

		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			RecommendationType.RELATED,
			recommendations
		);
	}

	private List<ProductRecommendationResponse> findFallbackRecommendations(
		ProductSnapshot sourceProduct,
		int size,
		String reason
	) {
		Map<Long, ProductRecommendationResponse> recommendationMap = new LinkedHashMap<>();
		int searchSize = Math.max(size * FALLBACK_SEARCH_MULTIPLIER, size);

		productRepository.findFallbackRelatedProductsByCategory(
				sourceProduct.getProductId(),
				sourceProduct.getCategoryId(),
				PageRequest.of(0, searchSize)
			)
			.stream()
			.filter(product -> RecommendationResultPolicy.isDifferentProduct(product, sourceProduct.getProductId()))
			.filter(RecommendationResultPolicy::isDisplayableProduct)
			.forEach(product -> recommendationMap.putIfAbsent(
				product.getProductId(),
				ProductRecommendationResponse.from(
					product,
					CATEGORY_FALLBACK_SCORE,
					RecommendationType.RELATED,
					reason
				)
			));

		if (recommendationMap.size() < size) {
			productRepository.findFallbackRelatedProducts(
					sourceProduct.getProductId(),
					PageRequest.of(0, searchSize)
				)
				.stream()
				.filter(product -> RecommendationResultPolicy.isDifferentProduct(product, sourceProduct.getProductId()))
				.filter(RecommendationResultPolicy::isDisplayableProduct)
				.forEach(product -> recommendationMap.putIfAbsent(
					product.getProductId(),
					ProductRecommendationResponse.from(
						product,
						GENERAL_FALLBACK_SCORE,
						RecommendationType.RELATED,
						reason
					)
				));
		}

		return RecommendationResultPolicy.finalizeProductRecommendations(
			recommendationMap.values().stream().toList(),
			size
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

	private double calculateFinalScore(
		double vectorScore,
		double rerankScore
	) {
		return clamp((rerankScore * 0.75) + (vectorScore * 0.25));
	}

	private double normalizeSimilarity(double similarityScore) {
		if (similarityScore <= 0) {
			return 0.0;
		}

		return Math.min(similarityScore, 1.0);
	}

	private double clamp(double score) {
		if (score < 0.0) {
			return 0.0;
		}

		return Math.min(score, 1.0);
	}

	private int normalizeSize(int size) {
		return requestValidator.normalizeSize(size, DEFAULT_SIZE, MAX_SIZE);
	}

	private record RelatedCandidate(
		ProductSnapshot product,
		double vectorScore
	) {
	}
}