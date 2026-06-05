package com.princesses7.findy.recommendation.external.shopping.dto;

import java.time.LocalDate;
import java.util.List;

public record FrequentPurchaseProductListResponse(
	Long userId,
	LocalDate fromDate,
	LocalDate toDate,
	int limit,
	List<FrequentPurchaseProductResponse> products
) {
}
