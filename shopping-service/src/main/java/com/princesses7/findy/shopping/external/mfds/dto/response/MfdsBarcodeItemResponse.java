package com.princesses7.findy.shopping.external.mfds.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsBarcodeItemResponse(

	@JsonProperty("BRCD_NO")
	String barcode,

	@JsonProperty("PRDLST_REPORT_NO")
	String reportNo,

	@JsonProperty("CMPNY_NM")
	String companyName,

	@JsonProperty("PRDT_NM")
	String productName,

	@JsonProperty("LAST_UPDT_DTM")
	String lastUpdatedAt,

	@JsonProperty("PRDLST_NM")
	String categorySmall,

	@JsonProperty("HRNK_PRDLST_NM")
	String categoryMiddle,

	@JsonProperty("HTRK_PRDLST_NM")
	String categoryLarge
) {
}