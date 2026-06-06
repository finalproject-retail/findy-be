package com.princesses7.findy.recommendation.chatbot.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ChatbotProductContextResponse(
	Long productId,
	String brandName,
	String productName,
	Long categoryId,
	String categoryName,
	Integer originalPrice,
	Integer salePrice,
	BigDecimal discountRate,
	String saleStatus,
	Integer stockQuantity,
	String stockStatus,
	String stockText,
	List<ChatbotPromotionContextResponse> promotions,
	List<ChatbotCouponContextResponse> coupons
) {
}