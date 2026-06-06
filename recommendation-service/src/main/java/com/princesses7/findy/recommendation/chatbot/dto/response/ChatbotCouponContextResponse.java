package com.princesses7.findy.recommendation.chatbot.dto.response;

public record ChatbotCouponContextResponse(
	Long couponId,
	String couponName,
	String couponType,
	String discountType,
	Integer discountValue,
	Integer minOrderAmount,
	String benefitText
) {
}