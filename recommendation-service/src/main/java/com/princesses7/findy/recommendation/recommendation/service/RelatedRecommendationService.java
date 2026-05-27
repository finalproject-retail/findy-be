package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.recommendation.embedding.entity.ProductEmbedding;
import com.princesses7.findy.recommendation.embedding.repository.ProductEmbeddingRepository;
import com.princesses7.findy.recommendation.embedding.util.VectorSimilarityCalculator;
import com.princesses7.findy.recommendation.external.openai.OpenAiEmbeddingClient;
import com.princesses7.findy.recommendation.external.openai.OpenAiRelatedProductRerankClient;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
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
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;
import com.princesses7.findy.recommendation.recommendation.validator.RecommendationRequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatedRecommendationService {

	private static final int DEFAULT_SIZE = 5;
	private static final int MAX_SIZE = 20;
	private static final int VECTOR_CANDIDATE_LIMIT = 30;

	private static final double MIN_VECTOR_SCORE = 0.35;
	private static final double MIN_RERANK_SCORE = 0.55;

	private final ProductSnapshotRepository productRepository;
	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final OpenAiEmbeddingClient openAiEmbeddingClient;
	private final OpenAiRelatedProductRerankClient rerankClient;
	private final OpenAiProperties openAiProperties;
	private final RecommendationRequestValidator requestValidator;

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

		List<ProductEmbedding> candidateEmbeddings = productEmbeddingRepository.findByModelAndDimensions(
			openAiProperties.embeddingModel(),
			openAiProperties.embeddingDimensions()
		);

		if (candidateEmbeddings.isEmpty()) {
			return emptyResponse(userId, sourceProduct, sourceCategoryName);
		}

		List<Long> candidateProductIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(candidateProductIds);

		if (productMap.isEmpty()) {
			return emptyResponse(userId, sourceProduct, sourceCategoryName);
		}

		Map<Long, String> categoryNameMap = findCategoryNameMap(
			productMap.values()
				.stream()
				.map(ProductSnapshot::getCategoryId)
				.toList()
		);

		List<Double> relatedIntentEmbedding = openAiEmbeddingClient.createEmbedding(
			sourceProduct.toRelatedRecommendationText(sourceCategoryName)
		);

		ProductEmbedding sourceEmbedding = productEmbeddingRepository.findByProductId(sourceProduct.getProductId())
			.orElse(null);

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
			return emptyResponse(userId, sourceProduct, sourceCategoryName);
		}

		List<ProductSnapshot> rerankTargets = vectorCandidates.stream()
			.map(RelatedCandidate::product)
			.toList();

		List<RelatedProductRerankItem> rerankItems = rerankClient.rerank(
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

		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			RecommendationType.RELATED,
			recommendations
		);
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

		double sourceSimilarity = 0.0;

		if (sourceEmbedding != null) {
			sourceSimilarity = VectorSimilarityCalculator.cosineSimilarity(
				sourceEmbedding.getEmbeddingVector(),
				candidateEmbedding.getEmbeddingVector()
			);
		}

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

		score += calculateDiscountScore(candidate.getDiscountRate());

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

		return vectorCandidates.stream()
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
			.sorted(
				Comparator.comparing(ProductRecommendationResponse::score)
					.reversed()
					.thenComparing(ProductRecommendationResponse::productId)
			)
			.limit(size)
			.toList();
	}

	private ProductRecommendationListResponse emptyResponse(
		Long userId,
		ProductSnapshot sourceProduct,
		String sourceCategoryName
	) {
		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			SourceProductResponse.from(sourceProduct, sourceCategoryName),
			RecommendationType.RELATED,
			List.of()
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

	private double calculateDiscountScore(BigDecimal discountRate) {
		if (discountRate == null) {
			return 0.0;
		}

		double rate = discountRate.doubleValue();

		if (rate >= 30) {
			return 0.05;
		}

		if (rate >= 20) {
			return 0.03;
		}

		if (rate >= 10) {
			return 0.02;
		}

		if (rate > 0) {
			return 0.01;
		}

		return 0.0;
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