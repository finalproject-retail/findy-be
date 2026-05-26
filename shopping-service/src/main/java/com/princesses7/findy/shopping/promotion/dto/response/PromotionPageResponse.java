package com.princesses7.findy.shopping.promotion.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record PromotionPageResponse(
	List<PromotionResponse> promotions,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean hasNext
) {

	public static PromotionPageResponse from(Page<PromotionResponse> page) {
		return new PromotionPageResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.hasNext()
		);
	}
}