package com.princesses7.findy.recommendation.recommendation.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionType;

@Component
public class PromotionRecommendationScoreCalculator {

	private static final double EMBEDDING_WEIGHT = 0.70;
	private static final double PROMOTION_BENEFIT_WEIGHT = 0.15;
	private static final double STOCK_WEIGHT = 0.05;
	private static final double PROMOTION_TYPE_WEIGHT = 0.05;
	private static final double LOCATION_WEIGHT = 0.05;

	public double calculate(
		double similarityScore,
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory
	) {
		return calculate(similarityScore, product, promotionProduct, inventory, null);
	}

	public double calculate(
		double similarityScore,
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		Long currentGridId
	) {
		double score = normalizeSimilarity(similarityScore) * EMBEDDING_WEIGHT;

		score += calculatePromotionBenefitScore(product, promotionProduct) * PROMOTION_BENEFIT_WEIGHT;
		score += calculateStockScore(inventory) * STOCK_WEIGHT;
		score += calculatePromotionTypeScore(promotionProduct.getPromotion()) * PROMOTION_TYPE_WEIGHT;
		score += calculateLocationScore(promotionProduct, currentGridId) * LOCATION_WEIGHT;

		return clamp(score);
	}

	public double calculateFallback(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory
	) {
		return calculateFallback(product, promotionProduct, inventory, null);
	}

	public double calculateFallback(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		Long currentGridId
	) {
		double score = 0.30;

		score += calculatePromotionBenefitScore(product, promotionProduct) * 0.35;
		score += calculateStockScore(inventory) * 0.15;
		score += calculatePromotionTypeScore(promotionProduct.getPromotion()) * 0.15;
		score += calculateLocationScore(promotionProduct, currentGridId) * 0.05;

		return clamp(score);
	}

	public String createReason(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		double similarityScore
	) {
		return createReason(product, promotionProduct, similarityScore, null);
	}

	public String createReason(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		double similarityScore,
		Long currentGridId
	) {
		PromotionSnapshot promotion = promotionProduct.getPromotion();

		if (isNearCurrentGrid(promotionProduct, currentGridId)) {
			return "사용자 선호 정보와 의미적으로 유사하고 현재 위치와 가까운 행사 상품이라 추천했습니다.";
		}

		if (promotionProduct.getPromotionPrice() != null) {
			return "사용자 선호 정보와 의미적으로 유사한 행사 상품이며, 행사 가격 혜택이 적용되어 추천했습니다.";
		}

		if (promotion.getPromotionType() == PromotionType.BOGO) {
			return "사용자 선호 정보와 의미적으로 유사한 묶음 행사 상품이라 추천했습니다.";
		}

		if (promotion.getPromotionType() == PromotionType.GIFT) {
			return "사용자 선호 정보와 의미적으로 유사한 사은품 행사 상품이라 추천했습니다.";
		}

		if (hasDiscount(product)) {
			return "사용자 선호 정보와 의미적으로 유사하고 할인 혜택이 있는 상품이라 추천했습니다.";
		}

		return "사용자 선호 정보와 행사 상품 정보를 AI 임베딩으로 비교해 추천했습니다.";
	}

	public String createFallbackReason(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct
	) {
		return createFallbackReason(product, promotionProduct, null);
	}

	public String createFallbackReason(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		Long currentGridId
	) {
		PromotionSnapshot promotion = promotionProduct.getPromotion();

		if (isNearCurrentGrid(promotionProduct, currentGridId)) {
			return "추천 데이터가 부족하여 현재 위치와 가까운 행사 상품을 우선 추천했습니다.";
		}

		if (promotionProduct.getPromotionPrice() != null) {
			return "추천 데이터가 부족하여 행사 가격과 재고를 기준으로 추천한 상품입니다.";
		}

		if (promotion.getPromotionType() == PromotionType.BOGO) {
			return "추천 데이터가 부족하여 현재 진행 중인 묶음 행사 상품을 우선 추천했습니다.";
		}

		if (promotion.getPromotionType() == PromotionType.GIFT) {
			return "추천 데이터가 부족하여 현재 진행 중인 사은품 행사 상품을 우선 추천했습니다.";
		}

		if (hasDiscount(product)) {
			return "추천 데이터가 부족하여 할인 혜택이 있는 행사 상품을 우선 추천했습니다.";
		}

		return "추천 데이터가 부족하여 구매 가능한 행사 상품을 기준으로 추천했습니다.";
	}

	private double normalizeSimilarity(double similarityScore) {
		return Math.max(0.0, Math.min((similarityScore + 1.0) / 2.0, 1.0));
	}

	private double calculatePromotionBenefitScore(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct
	) {
		if (promotionProduct.getPromotionPrice() != null
			&& product.getSalePrice() != null
			&& product.getSalePrice() > 0
			&& promotionProduct.getPromotionPrice() < product.getSalePrice()) {
			double discountRatio = 1 - promotionProduct.getPromotionPrice() / (double)product.getSalePrice();

			return clamp(discountRatio);
		}

		if (product.getDiscountRate() == null) {
			return 0.0;
		}

		return clamp(product.getDiscountRate().doubleValue() / 100.0);
	}

	private double calculateStockScore(InventorySnapshot inventory) {
		if (inventory == null || !inventory.hasAvailableStock()) {
			return 0.0;
		}

		if (inventory.getStockQuantity() == null) {
			return 0.0;
		}

		return clamp(inventory.getStockQuantity() / 100.0);
	}

	private double calculatePromotionTypeScore(PromotionSnapshot promotion) {
		if (promotion.getPromotionType() == PromotionType.BOGO) {
			return 1.0;
		}

		if (promotion.getPromotionType() == PromotionType.DISCOUNT) {
			return 0.9;
		}

		if (promotion.getPromotionType() == PromotionType.GIFT) {
			return 0.8;
		}

		return 0.0;
	}

	private double calculateLocationScore(
		PromotionProductSnapshot promotionProduct,
		Long currentGridId
	) {
		if (currentGridId == null || promotionProduct.getGridId() == null) {
			return 0.0;
		}

		long distance = Math.abs(promotionProduct.getGridId() - currentGridId);

		if (distance == 0) {
			return 1.0;
		}

		if (distance <= 2) {
			return 0.7;
		}

		if (distance <= 5) {
			return 0.4;
		}

		return 0.0;
	}

	private boolean isNearCurrentGrid(
		PromotionProductSnapshot promotionProduct,
		Long currentGridId
	) {
		return calculateLocationScore(promotionProduct, currentGridId) > 0.0;
	}

	private boolean hasDiscount(ProductSnapshot product) {
		return product.getDiscountRate() != null
			&& product.getDiscountRate().compareTo(BigDecimal.ZERO) > 0;
	}

	private double clamp(double score) {
		return Math.max(0.0, Math.min(score, 1.0));
	}
}