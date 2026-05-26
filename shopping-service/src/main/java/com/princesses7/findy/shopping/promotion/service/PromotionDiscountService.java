package com.princesses7.findy.shopping.promotion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.promotion.dto.response.PromotionDiscountResult;
import com.princesses7.findy.shopping.promotion.entity.Promotion;
import com.princesses7.findy.shopping.promotion.entity.PromotionProduct;
import com.princesses7.findy.shopping.promotion.entity.PromotionStatus;
import com.princesses7.findy.shopping.promotion.entity.PromotionType;
import com.princesses7.findy.shopping.promotion.repository.PromotionProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionDiscountService {

	private final PromotionProductRepository promotionProductRepository;

	public PromotionDiscountResult calculateBestDiscount(
		Long productId,
		int quantity,
		int productPrice,
		int totalAmount
	) {
		LocalDateTime now = LocalDateTime.now();

		List<PromotionProduct> promotionProducts = promotionProductRepository
			.findApplicablePromotionProducts(
				productId,
				PromotionStatus.ENDED,
				now
			);

		return promotionProducts.stream()
			.map(promotionProduct -> calculateDiscount(
				promotionProduct,
				quantity,
				productPrice,
				totalAmount
			))
			.filter(result -> result.discountAmount() > 0 || result.promotionId() != null)
			.max(Comparator.comparingInt(PromotionDiscountResult::discountAmount))
			.orElseGet(PromotionDiscountResult::none);
	}

	private PromotionDiscountResult calculateDiscount(
		PromotionProduct promotionProduct,
		int quantity,
		int productPrice,
		int totalAmount
	) {
		Promotion promotion = promotionProduct.getPromotion();

		if (promotion.getPromotionType() == PromotionType.DISCOUNT) {
			return calculateDiscountPromotion(
				promotionProduct,
				quantity,
				totalAmount
			);
		}

		if (promotion.getPromotionType() == PromotionType.BOGO) {
			return calculateBogoPromotion(
				promotionProduct,
				quantity,
				productPrice
			);
		}

		if (promotion.getPromotionType() == PromotionType.GIFT) {
			return calculateGiftPromotion(
				promotionProduct,
				totalAmount
			);
		}

		return PromotionDiscountResult.none();
	}

	private PromotionDiscountResult calculateDiscountPromotion(
		PromotionProduct promotionProduct,
		int quantity,
		int totalAmount
	) {
		Promotion promotion = promotionProduct.getPromotion();
		int discountAmount = 0;

		if (promotionProduct.getPromotionPrice() != null) {
			discountAmount = Math.max(
				totalAmount - promotionProduct.getPromotionPrice() * quantity,
				0
			);
		} else if (promotion.getDiscountRate() != null) {
			discountAmount = BigDecimal.valueOf(totalAmount)
				.multiply(promotion.getDiscountRate())
				.divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
				.intValue();
		}

		return toResult(promotion, discountAmount);
	}

	private PromotionDiscountResult calculateBogoPromotion(
		PromotionProduct promotionProduct,
		int quantity,
		int productPrice
	) {
		Promotion promotion = promotionProduct.getPromotion();
		Integer buyQuantity = promotion.getBuyQuantity();
		Integer getQuantity = promotion.getGetQuantity();

		if (buyQuantity == null || getQuantity == null) {
			return PromotionDiscountResult.none();
		}

		int bundleQuantity = buyQuantity + getQuantity;
		if (quantity < bundleQuantity) {
			return PromotionDiscountResult.none();
		}

		int freeQuantity = quantity / bundleQuantity * getQuantity;
		int discountAmount = freeQuantity * productPrice;

		return toResult(promotion, discountAmount);
	}

	private PromotionDiscountResult calculateGiftPromotion(
		PromotionProduct promotionProduct,
		int totalAmount
	) {
		Promotion promotion = promotionProduct.getPromotion();
		Integer minPurchaseAmount = promotion.getMinPurchaseAmount();

		if (minPurchaseAmount == null || totalAmount < minPurchaseAmount) {
			return PromotionDiscountResult.none();
		}

		return toResult(promotion, 0);
	}

	private PromotionDiscountResult toResult(
		Promotion promotion,
		int discountAmount
	) {
		return new PromotionDiscountResult(
			promotion.getPromotionId(),
			promotion.getPromotionName(),
			promotion.getPromotionType(),
			promotion.getBenefitText(),
			discountAmount
		);
	}
}