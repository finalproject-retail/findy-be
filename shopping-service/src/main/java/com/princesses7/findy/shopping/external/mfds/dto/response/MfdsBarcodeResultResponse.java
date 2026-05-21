package com.princesses7.findy.shopping.external.mfds.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsBarcodeResultResponse(

	@JsonProperty("CODE")
	String code,

	@JsonProperty("MSG")
	String message
) {
}