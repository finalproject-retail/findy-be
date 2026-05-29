package com.princesses7.findy.shopping.external.kca.dto.response;

import java.util.List;

public record KcaProductPriceResponse(
	String resultCode,
	String resultMessage,
	List<KcaProductPriceItemResponse> items
) {
}