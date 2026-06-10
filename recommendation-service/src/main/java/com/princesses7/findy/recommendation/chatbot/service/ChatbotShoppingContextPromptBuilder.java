package com.princesses7.findy.recommendation.chatbot.service;

import java.util.List;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotCouponContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotProductContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotPromotionContextResponse;
import com.princesses7.findy.recommendation.chatbot.dto.response.ChatbotShoppingContextResponse;
import com.princesses7.findy.recommendation.chatbot.entity.ChatIntent;

@Component
public class ChatbotShoppingContextPromptBuilder {

	public String build(ChatbotShoppingContextResponse shoppingContext) {
		if (shoppingContext == null) {
			return "";
		}

		if (!shoppingContext.hasData()) {
			return buildEmptyPrompt(shoppingContext);
		}

		StringBuilder builder = new StringBuilder();

		builder.append("""
			Findy 쇼핑 데이터 조회 결과:
			아래 데이터는 실제 서비스 DB에서 조회된 상품/재고/행사/쿠폰 정보이다.
			
			답변 지침:
			- 반드시 아래 데이터만 근거로 답변한다.
			- 재고 수량, 할인율, 행사명, 쿠폰명은 조회된 값만 말한다.
			- 데이터에 없는 혜택은 있다고 말하지 않는다.
			- 사용자가 재고를 물어보면 stockText를 우선 사용한다.
			- 사용자가 쿠폰을 물어보면 쿠폰 정보가 있는 상품만 중심으로 답한다.
			- 사용자가 행사/할인을 물어보면 행사 정보와 상품 할인율을 함께 안내한다.
			
			""");

		if (shoppingContext.intent() == ChatIntent.GENERAL_PRODUCT_RECOMMENDATION) {
			builder.append("""
				일반 상품 추천 답변 지침:
				- 사용자의 맛/취향/상황 요청에 맞는 상품을 3~5개 정도만 짧게 추천한다.
				- 상품명, 판매가, 재고 상태를 간단히 안내한다.
				- 실제 조회된 상품 목록에 없는 상품은 절대 만들지 않는다.
				- 프론트에서 상품 카드를 보여줄 수 있으므로 답변을 너무 길게 나열하지 않는다.
				
				""");
		}

		builder.append("조회 매장 ID: ").append(shoppingContext.storeId()).append('\n');
		builder.append("LLM 추출 키워드: ").append(nullToDash(shoppingContext.keyword())).append('\n');
		builder.append("LLM 분석 의도: ").append(shoppingContext.intent()).append('\n');
		builder.append('\n');

		List<ChatbotProductContextResponse> products = shoppingContext.products();

		for (int index = 0; index < products.size(); index++) {
			ChatbotProductContextResponse product = products.get(index);

			builder.append(index + 1).append(". 상품\n");
			builder.append("- productId: ").append(product.productId()).append('\n');
			builder.append("- 상품명: ").append(product.productName()).append('\n');
			builder.append("- 브랜드: ").append(nullToDash(product.brandName())).append('\n');
			builder.append("- 카테고리: ").append(nullToDash(product.categoryName())).append('\n');
			builder.append("- 정가: ").append(product.originalPrice()).append("원\n");
			builder.append("- 판매가: ").append(product.salePrice()).append("원\n");
			builder.append("- 할인율: ").append(product.discountRate()).append("%\n");
			builder.append("- 판매 상태: ").append(product.saleStatus()).append('\n');
			builder.append("- 재고 상태: ").append(product.stockText()).append('\n');

			appendPromotions(builder, product.promotions());
			appendCoupons(builder, product.coupons());

			builder.append('\n');
		}

		return builder.toString();
	}

	private String buildEmptyPrompt(ChatbotShoppingContextResponse shoppingContext) {
		StringBuilder builder = new StringBuilder();

		builder.append("""
			Findy 쇼핑 데이터 조회 결과:
			- 사용자의 질문과 관련된 상품/재고/행사/쿠폰 데이터를 찾지 못했다.
			
			답변 지침:
			- 실제로 조회된 상품이 없다고 안내한다.
			- 상품명이나 카테고리를 더 구체적으로 입력해 달라고 안내한다.
			- 존재하지 않는 상품, 재고, 쿠폰, 행사를 지어내지 않는다.
			""");

		if (shoppingContext.intent() == ChatIntent.GENERAL_PRODUCT_RECOMMENDATION) {
			builder.append("""
				
				일반 상품 추천 실패 답변 지침:
				- 조건에 맞는 추천 상품을 찾지 못했다고 짧게 안내한다.
				- 상품군이나 취향을 조금 더 구체적으로 입력해 달라고 안내한다.
				""");
		}

		return builder.toString();
	}

	private void appendPromotions(
		StringBuilder builder,
		List<ChatbotPromotionContextResponse> promotions
	) {
		if (promotions == null || promotions.isEmpty()) {
			builder.append("- 행사: 없음\n");
			return;
		}

		builder.append("- 행사:\n");

		for (ChatbotPromotionContextResponse promotion : promotions) {
			builder.append("  * ")
				.append(promotion.promotionName())
				.append(" / ")
				.append(promotion.benefitText());

			if (promotion.promotionPrice() != null) {
				builder.append(" / 행사 가격 ")
					.append(promotion.promotionPrice())
					.append("원");
			}

			builder.append('\n');
		}
	}

	private void appendCoupons(
		StringBuilder builder,
		List<ChatbotCouponContextResponse> coupons
	) {
		if (coupons == null || coupons.isEmpty()) {
			builder.append("- 쿠폰: 없음\n");
			return;
		}

		builder.append("- 쿠폰:\n");

		for (ChatbotCouponContextResponse coupon : coupons) {
			builder.append("  * ")
				.append(coupon.couponName())
				.append(" / ")
				.append(coupon.benefitText())
				.append(" / 최소 주문 금액 ")
				.append(coupon.minOrderAmount())
				.append("원")
				.append('\n');
		}
	}

	private String nullToDash(String value) {
		if (value == null || value.isBlank()) {
			return "-";
		}

		return value;
	}
}