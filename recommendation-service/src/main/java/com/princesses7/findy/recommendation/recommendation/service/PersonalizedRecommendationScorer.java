package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

@Component
public class PersonalizedRecommendationScorer {

	private static final List<String> FRESH_CATEGORY_KEYWORDS = List.of(
		"신선 식품",
		"농산",
		"과일",
		"채소",
		"샐러드",
		"축산",
		"수산",
		"유제품",
		"냉장"
	);

	private static final List<String> HEALTH_KEYWORDS = List.of(
		"건강",
		"유기농",
		"비건",
		"샐러드",
		"요거트",
		"단백질"
	);

	public double calculate(
		UserPreferenceResponse userPreference,
		ProductSnapshot product,
		String categoryName,
		double similarityScore
	) {
		double score = normalizeSimilarity(similarityScore) * 0.70;

		if (userPreference.hasPreferredCategory(product.getCategoryId())) {
			score += 0.15;
		}

		if (userPreference.hasShoppingStyle("가성비")) {
			score += calculateDiscountScore(product.getDiscountRate());
		}

		if (userPreference.hasShoppingStyle("신선도 중시") && containsAny(categoryName, FRESH_CATEGORY_KEYWORDS)) {
			score += 0.05;
		}

		if (userPreference.hasShoppingStyle("건강/유기농") && productContainsAny(product, HEALTH_KEYWORDS)) {
			score += 0.05;
		}

		if (userPreference.hasShoppingStyle("비건") && productContains(product, "비건")) {
			score += 0.05;
		}

		return Math.min(score, 1.0);
	}

	public double fallbackScore(ProductSnapshot product) {
		double score = 0.05;

		score += calculateDiscountScore(product.getDiscountRate());

		if (product.getSalePrice() != null && product.getSalePrice() > 0) {
			score += 0.05;
		}

		return Math.min(score, 1.0);
	}

	public double popularFallbackScore(
		ProductSnapshot product,
		long popularityScore,
		double maxPopularityScore
	) {
		double normalizedPopularityScore = 0.0;

		if (maxPopularityScore > 0) {
			normalizedPopularityScore = Math.min(popularityScore / maxPopularityScore, 1.0);
		}

		double score = normalizedPopularityScore * 0.85;
		score += calculateDiscountScore(product.getDiscountRate());

		if (product.getSalePrice() != null && product.getSalePrice() > 0) {
			score += 0.05;
		}

		return Math.min(score, 1.0);
	}

	public String createReason(
		UserPreferenceResponse userPreference,
		ProductSnapshot product,
		String categoryName
	) {
		if (userPreference.hasPreferredCategory(product.getCategoryId())) {
			return "첫 로그인 설문에서 선택한 선호 카테고리를 기반으로 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("가성비") && hasDiscount(product)) {
			return "가성비를 중시하는 쇼핑 스타일과 할인 정보를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("신선도 중시") && containsAny(categoryName, FRESH_CATEGORY_KEYWORDS)) {
			return "신선도를 중시하는 쇼핑 스타일과 상품 카테고리를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("건강/유기농") && productContainsAny(product, HEALTH_KEYWORDS)) {
			return "건강/유기농을 선호하는 쇼핑 스타일과 상품 정보를 반영해 추천한 상품입니다.";
		}

		return "첫 로그인 설문에서 선택한 선호 카테고리와 쇼핑 스타일을 기반으로 추천한 상품입니다.";
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
			return 0.10;
		}

		if (rate >= 20) {
			return 0.08;
		}

		if (rate >= 10) {
			return 0.05;
		}

		if (rate > 0) {
			return 0.03;
		}

		return 0.0;
	}

	private boolean hasDiscount(ProductSnapshot product) {
		return product.getDiscountRate() != null
			&& product.getDiscountRate().compareTo(BigDecimal.ZERO) > 0;
	}

	private boolean containsAny(
		String target,
		List<String> keywords
	) {
		if (target == null || target.isBlank()) {
			return false;
		}

		return keywords.stream()
			.anyMatch(target::contains);
	}

	private boolean productContainsAny(
		ProductSnapshot product,
		List<String> keywords
	) {
		return keywords.stream()
			.anyMatch(keyword -> productContains(product, keyword));
	}

	private boolean productContains(
		ProductSnapshot product,
		String keyword
	) {
		String target = String.join(" ",
			nullToEmpty(product.getProductName()),
			nullToEmpty(product.getBrandName()),
			nullToEmpty(product.getDescription()),
			nullToEmpty(product.getBadgeText())
		);

		return target.contains(keyword);
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}