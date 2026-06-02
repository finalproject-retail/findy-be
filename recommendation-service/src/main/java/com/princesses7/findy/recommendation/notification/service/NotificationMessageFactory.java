package com.princesses7.findy.recommendation.notification.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.princesses7.findy.recommendation.recommendation.dto.response.ProductRecommendationResponse;
import com.princesses7.findy.recommendation.recommendation.dto.response.PromotionProductRecommendationResponse;

@Component
public class NotificationMessageFactory {

	public NotificationMessage createRelatedProductMessage(
		String sourceProductName,
		ProductRecommendationResponse recommendation
	) {
		String title = "🎁 방금 담은 " + sourceProductName + "와 어울리는 상품이에요!";
		String content = "[" + recommendation.productName() + "]도 함께 많이 찾는 상품이에요."
			+ createDiscountText(recommendation.discountRate());

		return new NotificationMessage(title, content);
	}

	public NotificationMessage createPersonalizedPromotionMessage(
		PromotionProductRecommendationResponse recommendation
	) {
		String title = "🎁 취향에 맞는 행사 상품이 있어요!";
		String content = "선호하실 만한 [" + recommendation.productName() + "] "
			+ recommendation.benefitText() + " 혜택을 확인해보세요.";

		return new NotificationMessage(title, content);
	}

	private String createDiscountText(BigDecimal discountRate) {
		if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0) {
			return "";
		}

		return " 지금 구매하면 " + discountRate.stripTrailingZeros().toPlainString() + "% 할인!";
	}
}