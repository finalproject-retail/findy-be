package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
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
import com.princesses7.findy.recommendation.external.openai.OpenAiEmbeddingClient;
import com.princesses7.findy.recommendation.global.config.OpenAiProperties;
import com.princesses7.findy.recommendation.global.exception.BaseException;
import com.princesses7.findy.recommendation.global.exception.ErrorCode;
import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;
import com.princesses7.findy.recommendation.preference.repository.CategorySnapshotRepository;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.product.repository.ProductSnapshotRepository;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationListResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelatedRecommendationService {

	private static final int DEFAULT_SIZE = 5;
	private static final int MAX_SIZE = 20;
	private static final int FALLBACK_MULTIPLIER = 3;

	private final ProductEmbeddingRepository productEmbeddingRepository;
	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;
	private final OpenAiEmbeddingClient openAiEmbeddingClient;
	private final OpenAiProperties openAiProperties;

	public ProductRecommendationListResponse getRelatedRecommendations(
		Long userId,
		Long productId,
		int size
	) {
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
			return fallback(userId, sourceProduct, normalizedSize);
		}

		List<Long> candidateProductIds = candidateEmbeddings.stream()
			.map(ProductEmbedding::getProductId)
			.toList();

		Map<Long, ProductSnapshot> productMap = findRecommendableProductMap(candidateProductIds);

		if (productMap.isEmpty()) {
			return fallback(userId, sourceProduct, normalizedSize);
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

		List<ProductRecommendationResponse> recommendations = candidateEmbeddings.stream()
			.map(candidateEmbedding -> toRecommendationResponse(
				sourceProduct,
				candidateEmbedding,
				productMap,
				categoryNameMap,
				relatedIntentEmbedding
			))
			.filter(Objects::nonNull)
			.sorted(
				Comparator.comparing(ProductRecommendationResponse::score)
					.reversed()
					.thenComparing(ProductRecommendationResponse::productId)
			)
			.limit(normalizedSize)
			.toList();

		if (recommendations.isEmpty()) {
			return fallback(userId, sourceProduct, normalizedSize);
		}

		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			RecommendationType.RELATED,
			recommendations
		);
	}

	private ProductRecommendationResponse toRecommendationResponse(
		ProductSnapshot sourceProduct,
		ProductEmbedding candidateEmbedding,
		Map<Long, ProductSnapshot> productMap,
		Map<Long, String> categoryNameMap,
		List<Double> relatedIntentEmbedding
	) {
		ProductSnapshot candidate = productMap.get(candidateEmbedding.getProductId());

		if (candidate == null || candidate.getProductId().equals(sourceProduct.getProductId())) {
			return null;
		}

		double similarityScore = VectorSimilarityCalculator.cosineSimilarity(
			relatedIntentEmbedding,
			candidateEmbedding.getEmbeddingVector()
		);

		double score = calculateScore(
			sourceProduct,
			candidate,
			categoryNameMap.getOrDefault(candidate.getCategoryId(), ""),
			similarityScore
		);

		return ProductRecommendationResponse.from(
			candidate,
			score,
			RecommendationType.RELATED,
			createReason(sourceProduct, candidate)
		);
	}

	private double calculateScore(
		ProductSnapshot sourceProduct,
		ProductSnapshot candidate,
		String candidateCategoryName,
		double similarityScore
	) {
		double score = normalizeSimilarity(similarityScore) * 0.85;

		if (candidate.getCategoryId().equals(sourceProduct.getCategoryId())) {
			score -= 0.08;
		} else {
			score += 0.08;
		}

		if (candidateCategoryName != null && !candidateCategoryName.isBlank()) {
			score += 0.02;
		}

		score += calculateDiscountScore(candidate.getDiscountRate());

		return clamp(score);
	}

	private ProductRecommendationListResponse fallback(
		Long userId,
		ProductSnapshot sourceProduct,
		int size
	) {
		List<ProductRecommendationResponse> recommendations = productRepository.findByDeletedFalse(
				PageRequest.of(0, size * FALLBACK_MULTIPLIER)
			)
			.stream()
			.filter(ProductSnapshot::isRecommendable)
			.filter(product -> !product.getProductId().equals(sourceProduct.getProductId()))
			.sorted(
				Comparator.comparing(this::getDiscountRate)
					.reversed()
					.thenComparing(ProductSnapshot::getProductId)
			)
			.limit(size)
			.map(product -> ProductRecommendationResponse.from(
				product,
				calculateFallbackScore(product),
				RecommendationType.RELATED,
				"연관 상품 임베딩 데이터가 부족하여 구매 가능한 상품을 기준으로 추천합니다."
			))
			.toList();

		return new ProductRecommendationListResponse(
			userId,
			sourceProduct.getProductId(),
			RecommendationType.RELATED,
			recommendations
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

	private String createReason(
		ProductSnapshot sourceProduct,
		ProductSnapshot candidate
	) {
		if (!candidate.getCategoryId().equals(sourceProduct.getCategoryId())) {
			return "현재 상품과 함께 구매하기 좋은 보완 상품으로 AI 유사도 기반 추천되었습니다.";
		}

		if (hasDiscount(candidate)) {
			return "현재 상품과 함께 살펴볼 만한 할인 상품으로 AI 유사도 기반 추천되었습니다.";
		}

		return "현재 상품의 정보와 쇼핑 맥락을 반영해 AI 유사도 기반으로 추천되었습니다.";
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
			return 0.07;
		}

		if (rate >= 20) {
			return 0.05;
		}

		if (rate >= 10) {
			return 0.03;
		}

		if (rate > 0) {
			return 0.01;
		}

		return 0.0;
	}

	private double calculateFallbackScore(ProductSnapshot product) {
		double score = 0.05;

		if (hasDiscount(product)) {
			score += calculateDiscountScore(product.getDiscountRate());
		}

		if (product.getSalePrice() != null && product.getSalePrice() > 0) {
			score += 0.03;
		}

		return clamp(score);
	}

	private boolean hasDiscount(ProductSnapshot product) {
		return product.getDiscountRate() != null
			&& product.getDiscountRate().compareTo(BigDecimal.ZERO) > 0;
	}

	private BigDecimal getDiscountRate(ProductSnapshot product) {
		if (product.getDiscountRate() == null) {
			return BigDecimal.ZERO;
		}

		return product.getDiscountRate();
	}

	private double clamp(double score) {
		if (score < 0.0) {
			return 0.0;
		}

		return Math.min(score, 1.0);
	}

	private int normalizeSize(int size) {
		if (size <= 0) {
			return DEFAULT_SIZE;
		}

		return Math.min(size, MAX_SIZE);
	}
}