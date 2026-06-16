package com.princesses7.findy.recommendation.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.preference.dto.response.UserPreferenceResponse;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;

@Component
public class PersonalizedRecommendationScorer {

	// TODO: LLM으로 상품 태그 자동 분류하도록 수정
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
		double similarityScore,
		PurchaseHistoryContext purchaseHistory
	) {
		double score = normalizeSimilarity(similarityScore) * 0.55;

		if (userPreference.hasPreferredCategory(product.getCategoryId())) {
			score += 0.15;
		}

		if (purchaseHistory.hasHistory()) {
			score += purchaseHistory.productAffinity(product) * 0.20;
			score += purchaseHistory.categoryAffinity(product) * 0.10;
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
		return fallbackScore(product, null);
	}

	public double fallbackScore(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct
	) {
		double score = 0.05;

		// promotion_price 기준 할인 혜택 반영
		score += calculatePromotionBenefitScore(product, promotionProduct) * 0.10;

		return Math.min(score, 1.0);
	}

	public double popularFallbackScore(
		ProductSnapshot product,
		long popularityScore,
		double maxPopularityScore
	) {
		return popularFallbackScore(product, null, popularityScore, maxPopularityScore);
	}

	public double popularFallbackScore(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		long popularityScore,
		double maxPopularityScore
	) {
		double normalizedPopularityScore = 0.0;

		if (maxPopularityScore > 0) {
			normalizedPopularityScore = Math.min(popularityScore / maxPopularityScore, 1.0);
		}

		double score = normalizedPopularityScore * 0.85;

		// 인기 fallback에서도 행사 혜택 약간 반영
		score += calculatePromotionBenefitScore(product, promotionProduct) * 0.10;

		return Math.min(score, 1.0);
	}

	public String createReason(
		UserPreferenceResponse userPreference,
		ProductSnapshot product,
		String categoryName,
		PurchaseHistoryContext purchaseHistory
	) {
		if (purchaseHistory.productAffinity(product) > 0) {
			return "최근 구매 이력에서 반복 구매한 상품을 반영해 추천한 상품입니다.";
		}

		if (purchaseHistory.categoryAffinity(product) > 0) {
			return "최근 구매 이력에서 자주 구매한 카테고리를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasPreferredCategory(product.getCategoryId())) {
			return "첫 로그인 설문에서 선택한 선호 카테고리를 기반으로 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("신선도 중시") && containsAny(categoryName, FRESH_CATEGORY_KEYWORDS)) {
			return "신선도를 중시하는 쇼핑 스타일과 상품 카테고리를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("건강/유기농") && productContainsAny(product, HEALTH_KEYWORDS)) {
			return "건강/유기농을 선호하는 쇼핑 스타일과 상품 정보를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("비건") && productContains(product, "비건")) {
			return "비건 쇼핑 스타일과 상품 정보를 반영해 추천한 상품입니다.";
		}

		if (userPreference.hasShoppingStyle("가성비")) {
			return "가성비를 중시하는 쇼핑 스타일과 상품 선호 정보를 기반으로 추천한 상품입니다.";
		}

		return "첫 로그인 설문에서 선택한 선호 카테고리와 쇼핑 스타일을 기반으로 추천한 상품입니다.";
	}

	private double calculatePromotionBenefitScore(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct
	) {
		if (product == null || promotionProduct == null) {
			return 0.0;
		}

		Integer originalPrice = product.getOriginalPrice();
		Integer promotionPrice = promotionProduct.getPromotionPrice();

		if (originalPrice == null || promotionPrice == null || originalPrice <= 0) {
			return 0.0;
		}

		if (promotionPrice <= 0 || promotionPrice >= originalPrice) {
			return 0.0;
		}

		double discountRate = (originalPrice - promotionPrice) / (double)originalPrice;

		return Math.min(discountRate, 1.0);
	}

	private double normalizeSimilarity(double similarityScore) {
		if (similarityScore <= 0) {
			return 0.0;
		}

		return Math.min(similarityScore, 1.0);
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