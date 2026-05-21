package com.princesses7.findy.shopping.external.mfds.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsLinkedProductItemResponse(

	@JsonProperty("PRDLST_REPORT_NO")
	String reportNo,

	@JsonProperty("PRMS_DT")
	String permissionDate,

	@JsonProperty("END_DT")
	String endDate,

	@JsonProperty("PRDLST_NM")
	String productName,

	@JsonProperty("POG_DAYCNT")
	String expirationPeriod,

	@JsonProperty("PRDLST_DCNM")
	String foodType,

	@JsonProperty("BSSH_NM")
	String companyName,

	@JsonProperty("INDUTY_NM")
	String businessType,

	@JsonProperty("SITE_ADDR")
	String siteAddress,

	@JsonProperty("CLSBIZ_DT")
	String closedDate,

	@JsonProperty("BAR_CD")
	String barcode
) {
}