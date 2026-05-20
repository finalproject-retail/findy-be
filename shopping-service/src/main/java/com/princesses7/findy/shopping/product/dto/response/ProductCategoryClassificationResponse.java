package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;

public record ProductCategoryClassificationResponse(
	Long categoryId,
	String categoryPath,
	BigDecimal confidence,
	boolean reviewRequired,
	String reason
) {
}