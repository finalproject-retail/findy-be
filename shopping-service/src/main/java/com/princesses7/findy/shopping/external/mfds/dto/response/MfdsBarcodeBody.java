package com.princesses7.findy.shopping.external.mfds.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsBarcodeBody(

	@JsonProperty("total_count")
	String totalCount,

	@JsonProperty("row")
	List<MfdsBarcodeItemResponse> row,

	@JsonProperty("RESULT")
	MfdsBarcodeResultResponse result
) {
}