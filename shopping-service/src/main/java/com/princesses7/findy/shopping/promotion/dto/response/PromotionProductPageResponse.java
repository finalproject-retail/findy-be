package com.princesses7.findy.shopping.promotion.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PromotionProductPageResponse(
	List<PromotionProductResponse> promotionProducts,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {

	public static PromotionProductPageResponse from(Page<PromotionProductResponse> page) {
		return new PromotionProductPageResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext()
		);
	}
}