package com.princesses7.findy.shopping.order.dto.response;

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