package com.princesses7.findy.shopping.external.haccp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HaccpProductApiResponse(

	@JsonProperty("header")
	HaccpProductHeaderResponse header,

	@JsonProperty("body")
	HaccpProductBodyResponse body
) {
}