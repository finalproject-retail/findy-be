package com.princesses7.findy.shopping.external.naver.dto.response;

import java.util.List;

public record NaverShoppingResponse(
	String lastBuildDate,
	int total,
	int start,
	int display,
	List<NaverShoppingItemResponse> items
) {
}