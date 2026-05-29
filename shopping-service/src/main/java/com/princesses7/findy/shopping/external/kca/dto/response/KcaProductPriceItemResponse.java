package com.princesses7.findy.shopping.external.kca.dto.response;

public record KcaProductPriceItemResponse(
	String goodInspectDay,
	String goodId,
	String goodName,
	String entpId,
	String entpName,
	String productEntpCode,
	String productEntpName,
	String goodPrice,
	String plusoneYn,
	String saleYn,
	String goodDcYn,
	String goodDcStartDay,
	String goodDcEndDay,
	String inputDay
) {
}