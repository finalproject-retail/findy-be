package com.princesses7.findy.shopping.external.haccp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HaccpProductHeaderResponse(

	@JsonProperty("resultCode")
	String resultCode,

	@JsonProperty("resultMsg")
	String resultMessage
) {
}