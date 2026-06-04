package com.princesses7.findy.shopping.external.kca.dto.response;

import java.util.List;

public record KcaProductInfoResponse(
	String resultCode,
	String resultMessage,
	List<KcaProductInfoItemResponse> items
) {
}