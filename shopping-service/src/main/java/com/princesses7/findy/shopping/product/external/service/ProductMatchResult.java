package com.princesses7.findy.shopping.product.external.service;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchedBy;

public record ProductMatchResult(
	BigDecimal confidence,
	ProductExternalMatchedBy matchedBy,
	String reason
) {

	public boolean isBetterThan(ProductMatchResult other) {
		return other == null || confidence.compareTo(other.confidence()) > 0;
	}
}