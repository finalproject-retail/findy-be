package com.princesses7.findy.recommendation.recommendation.service;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionProductSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionSnapshot;
import com.princesses7.findy.recommendation.promotion.entity.PromotionType;

@Component
public class PromotionRecommendationReasonBuilder {

	public String createAiReason(
		PromotionProductSnapshot promotionProduct,
		PromotionRecommendationScoreResult scoreResult
	) {
		PromotionSnapshot promotion = promotionProduct.getPromotion();

		if (scoreResult.hasStrongAiMatch() && scoreResult.hasLocationSignal()) {
			return "사용자 선호 정보와 의미적으로 잘 맞고 현재 위치와 가까운 행사 상품이라 추천했습니다.";
		}

		if (scoreResult.hasStrongAiMatch() && scoreResult.hasPromotionBenefit()) {
			return "사용자 선호 정보와 의미적으로 잘 맞고 행사 혜택이 있는 상품이라 추천했습니다.";
		}

		if (promotionProduct.getPromotionPrice() != null) {
			return "사용자 선호 정보와 유사한 상품이며 행사 가격 혜택이 적용되어 추천했습니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.BOGO) {
			return "사용자 선호 정보와 유사한 묶음 행사 상품이라 추천했습니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.GIFT) {
			return "사용자 선호 정보와 유사한 사은품 행사 상품이라 추천했습니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.DISCOUNT) {
			return "사용자 선호 정보와 유사한 할인 행사 상품이라 추천했습니다.";
		}

		if (scoreResult.hasEnoughStock()) {
			return "사용자 선호 정보와 유사하고 재고가 충분한 행사 상품이라 추천했습니다.";
		}

		return "사용자 선호 정보와 행사 상품 정보를 AI 임베딩으로 비교해 추천했습니다.";
	}

	public String createFallbackReason(
		ProductSnapshot product,
		PromotionProductSnapshot promotionProduct,
		PromotionRecommendationScoreResult scoreResult
	) {
		PromotionSnapshot promotion = promotionProduct.getPromotion();

		if (scoreResult.hasLocationSignal()) {
			return "AI 추천 데이터가 부족하여 현재 위치와 가까운 행사 상품을 우선 추천했습니다.";
		}

		if (promotionProduct.getPromotionPrice() != null) {
			return "AI 추천 데이터가 부족하여 행사 가격과 재고를 기준으로 추천한 상품입니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.BOGO) {
			return "AI 추천 데이터가 부족하여 현재 진행 중인 묶음 행사 상품을 우선 추천했습니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.GIFT) {
			return "AI 추천 데이터가 부족하여 현재 진행 중인 사은품 행사 상품을 우선 추천했습니다.";
		}

		if (promotion != null && promotion.getPromotionType() == PromotionType.DISCOUNT) {
			return "AI 추천 데이터가 부족하여 현재 진행 중인 할인 행사 상품을 우선 추천했습니다.";
		}

		return "AI 추천 데이터가 부족하여 구매 가능한 행사 상품을 기준으로 추천했습니다.";
	}
}