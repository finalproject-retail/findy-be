package com.princesses7.findy.recommendation.chatbot.dto;

import java.math.BigDecimal;

public record ChatbotShoppingProduct(
	Long productId,
	Long categoryId,
	String categoryName,
	String brandName,
	String productName,
	String imageUrl,
	Integer originalPrice,
	String salesUnit,
	String volume,
	String allergyInfo,
	String badgeText,
	String saleStatus,
	Long gridId,
	Integer stockQuantity,
	String stockStatus
) {

	private static final String ON_SALE = "ON_SALE";
	private static final String SOLD_OUT = "SOLD_OUT";
	private static final String DISCONTINUED = "DISCONTINUED";

	public Integer salePrice() {
		return originalPrice;
	}

	public BigDecimal discountRate() {
		return BigDecimal.ZERO;
	}

	public boolean isRecommendable() {
		return ON_SALE.equals(saleStatus)
			&& !SOLD_OUT.equals(saleStatus)
			&& !DISCONTINUED.equals(saleStatus);
	}
}