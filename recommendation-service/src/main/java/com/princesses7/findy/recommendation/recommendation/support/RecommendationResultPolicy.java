package com.princesses7.findy.recommendation.recommendation.support;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;

public final class RecommendationResultPolicy {

	private static final double MIN_SCORE = 0.001;

	private RecommendationResultPolicy() {
	}

	public static boolean isDisplayableProduct(ProductSnapshot product) {
		return product != null && product.isRecommendable();
	}

	public static boolean isDifferentProduct(
		ProductSnapshot product,
		Long sourceProductId
	) {
		return product != null
			&& (sourceProductId == null || !sourceProductId.equals(product.getProductId()));
	}

	public static List<ProductRecommendationResponse> finalizeProductRecommendations(
		List<ProductRecommendationResponse> recommendations,
		int size
	) {
		if (recommendations == null || recommendations.isEmpty()) {
			return List.of();
		}

		return recommendations.stream()
			.filter(Objects::nonNull)
			.filter(recommendation -> isValidScore(recommendation.score()))
			.sorted(productRecommendationComparator())
			.filter(distinctBy(ProductRecommendationResponse::productId))
			.limit(size)
			.toList();
	}

	public static List<PromotionProductRecommendationResponse> finalizePromotionRecommendations(
		List<PromotionProductRecommendationResponse> recommendations,
		int size
	) {
		if (recommendations == null || recommendations.isEmpty()) {
			return List.of();
		}

		return recommendations.stream()
			.filter(Objects::nonNull)
			.filter(recommendation -> isValidScore(recommendation.score()))
			.filter(recommendation -> recommendation.stockQuantity() != null)
			.filter(recommendation -> recommendation.stockQuantity() > 0)
			.sorted(promotionRecommendationComparator())
			.filter(distinctBy(PromotionProductRecommendationResponse::productId))
			.limit(size)
			.toList();
	}

	private static boolean isValidScore(double score) {
		return !Double.isNaN(score)
			&& !Double.isInfinite(score)
			&& score >= MIN_SCORE;
	}

	private static Comparator<ProductRecommendationResponse> productRecommendationComparator() {
		return Comparator
			.comparingDouble(ProductRecommendationResponse::score)
			.reversed()
			.thenComparing(
				recommendation -> defaultDiscountRate(recommendation.discountRate()),
				Comparator.reverseOrder()
			)
			.thenComparingLong(recommendation -> defaultProductId(recommendation.productId()));
	}

	private static Comparator<PromotionProductRecommendationResponse> promotionRecommendationComparator() {
		return Comparator
			.comparingDouble(PromotionProductRecommendationResponse::score)
			.reversed()
			.thenComparing(
				recommendation -> defaultDiscountRate(recommendation.discountRate()),
				Comparator.reverseOrder()
			)
			.thenComparing(
				recommendation -> defaultStockQuantity(recommendation.stockQuantity()),
				Comparator.reverseOrder()
			)
			.thenComparingLong(recommendation -> defaultProductId(recommendation.productId()));
	}

	private static BigDecimal defaultDiscountRate(BigDecimal discountRate) {
		if (discountRate == null) {
			return BigDecimal.ZERO;
		}

		return discountRate;
	}

	private static Integer defaultStockQuantity(Integer stockQuantity) {
		if (stockQuantity == null) {
			return 0;
		}

		return stockQuantity;
	}

	private static long defaultProductId(Long productId) {
		if (productId == null) {
			return Long.MAX_VALUE;
		}

		return productId;
	}

	private static <T> Predicate<T> distinctBy(Function<? super T, ?> keyExtractor) {
		Set<Object> seen = new HashSet<>();

		return value -> seen.add(keyExtractor.apply(value));
	}
}