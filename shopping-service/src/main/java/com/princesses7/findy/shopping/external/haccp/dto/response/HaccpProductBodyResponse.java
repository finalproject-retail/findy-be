package com.princesses7.findy.shopping.external.haccp.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record HaccpProductBodyResponse(

	@JsonProperty("items")
	List<HaccpProductItemResponse> items,

	@JsonProperty("pageNo")
	Integer pageNo,

	@JsonProperty("numOfRows")
	Integer numOfRows,

	@JsonProperty("totalCount")
	Integer totalCount
) {
}