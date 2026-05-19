package com.princesses7.findy.shopping.external.mfds.dto.response;

public record MfdsBarcodeItemResponse(
	String PRDLST_REPORT_NO,
	String PRMS_DT,
	String END_DT,
	String PRDLST_NM,
	String POG_DAYCNT,
	String PRDLST_DCNM,
	String BSSH_NM,
	String INDUTY_NM,
	String SITE_ADDR,
	String CLSBIZ_DT,
	String BAR_CD
) {
}