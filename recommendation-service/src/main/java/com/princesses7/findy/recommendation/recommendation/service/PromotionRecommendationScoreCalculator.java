package com.princesses7.findy.recommendation.recommendation.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;
import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PromotionRecommendationScoreCalculator {

	private final PromotionRecommendationScoreProperties properties;

	public PromotionRecommendationScoreResult calculate(
		double similarityScore,
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory
	) {
		return calculate(similarityScore, product, promotionProduct, inventory, null);
	}

	public PromotionRecommendationScoreResult calculate(
		double similarityScore,
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		Long currentGridId
	) {
		double embeddingScore = normalizeSimilarity(similarityScore);
		double promotionBenefitScore = calculatePromotionBenefitScore(product, promotionProduct);
		double stockScore = calculateStockScore(inventory);
		double promotionTypeScore = calculatePromotionTypeScore(promotionProduct.getPromotion());
		double locationScore = calculateLocationScore(promotionProduct, currentGridId);

		double weightedScore =
			embeddingScore * properties.embeddingWeight()
				+ promotionBenefitScore * properties.promotionBenefitWeight()
				+ stockScore * properties.stockWeight()
				+ promotionTypeScore * properties.promotionTypeWeight()
				+ locationScore * properties.locationWeight();

		double totalScore = weightedScore / properties.activeWeightSum();

		return new PromotionRecommendationScoreResult(
			clamp(totalScore),
			embeddingScore,
			promotionBenefitScore,
			stockScore,
			promotionTypeScore,
			locationScore
		);
	}

	public PromotionRecommendationScoreResult calculateFallback(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory
	) {
		return calculateFallback(product, promotionProduct, inventory, null);
	}

	public PromotionRecommendationScoreResult calculateFallback(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		InventorySnapshot inventory,
		Long currentGridId
	) {
		double promotionBenefitScore = calculatePromotionBenefitScore(product, promotionProduct);
		double stockScore = calculateStockScore(inventory);
		double promotionTypeScore = calculatePromotionTypeScore(promotionProduct.getPromotion());
		double locationScore = calculateLocationScore(promotionProduct, currentGridId);

		double totalScore = properties.fallbackBaseScore()
			+ promotionBenefitScore * properties.fallbackBenefitWeight()
			+ stockScore * properties.fallbackStockWeight()
			+ promotionTypeScore * properties.fallbackPromotionTypeWeight()
			+ locationScore * properties.fallbackLocationWeight();

		return new PromotionRecommendationScoreResult(
			clamp(totalScore),
			0.0,
			promotionBenefitScore,
			stockScore,
			promotionTypeScore,
			locationScore
		);
	}

	private double normalizeSimilarity(double similarityScore) {
		return Math.max(0.0, Math.min((similarityScore + 1.0) / 2.0, 1.0));
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

		if (originalPrice != null
			&& originalPrice > 0
			&& promotionPrice != null
			&& promotionPrice > 0
			&& promotionPrice < originalPrice) {
			double discountRatio = 1 - promotionPrice / (double)originalPrice;

			return clamp(discountRatio);
		}

		PromotionSnapshot promotion = promotionProduct.getPromotion();

		if (promotion == null || promotion.getDiscountRate() == null) {
			return 0.0;
		}

		return clamp(promotion.getDiscountRate().doubleValue() / 100.0);
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
		if (promotion == null || promotion.getPromotionType() == null) {
			return 0.0;
		}

		if (promotion.getPromotionType().isBogoLike()) {
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

		if (distance <= properties.nearGridDistance()) {
			return 0.7;
		}

		if (distance <= properties.displayableGridDistance()) {
			return 0.4;
		}

		return 0.0;
	}

	private double clamp(double score) {
		return Math.max(0.0, Math.min(score, 1.0));
	}
}
