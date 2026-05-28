package com.princesses7.findy.shopping.product.dto.response;

import java.math.BigDecimal;

public record ProductCategoryClassificationResponse(
	Long categoryId,
	String categoryPath,
	BigDecimal confidence,
	boolean reviewRequired,
	String reason
) {

	public static ProductCategoryClassificationResponse failed(String reason) {
		return new ProductCategoryClassificationResponse(
			null,
			null,
			BigDecimal.ZERO,
			true,
			reason
		);
	}

	public boolean isClassified() {
		return categoryId != null
			&& confidence != null
			&& confidence.compareTo(BigDecimal.ZERO) > 0;
	}
}