package com.princesses7.findy.shopping.external.mfds.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsLinkedProductBody(

	@JsonProperty("total_count")
	String totalCount,

	@JsonProperty("row")
	List<MfdsLinkedProductItemResponse> row,

	@JsonProperty("RESULT")
	MfdsResultResponse result
) {
}