package com.princesses7.findy.shopping.admin.product.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

public record AdminProductPageResponse(
	List<AdminProductResponse> products,
	int page,
	int size,
	long totalElements,
	int totalPages,
	boolean first,
	boolean last
) {

	public static AdminProductPageResponse from(Page<AdminProductResponse> page) {
		return new AdminProductPageResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages(),
			page.isFirst(),
			page.isLast()
		);
	}
}