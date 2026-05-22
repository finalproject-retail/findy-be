package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	private final ProductSnapshotRepository productRepository;
	private final CategorySnapshotRepository categoryRepository;

	public ProductRecommendationListResponse getRelatedRecommendations(
		Long userId,
		Long productId,
		int size
	) {
		int normalizedSize = normalizeSize(size);

		ProductSnapshot sourceProduct = productRepository.findById(productId)
			.orElseThrow(() -> new BaseException(ErrorCode.RECOMMENDATION_PRODUCT_NOT_FOUND));

		categoryRepository.findById(sourceProduct.getCategoryId())
			.map(CategorySnapshot::getCategoryName)
			.orElse("");

		return fallback(userId, sourceProduct, normalizedSize);
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