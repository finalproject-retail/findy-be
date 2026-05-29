package com.princesses7.findy.shopping.external.haccp.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HaccpProductItemResponse(

	@JsonProperty("prdlstReportNo")
	String productReportNo,

	@JsonProperty("livestockFoodDiv")
	String livestockFoodDivision,

	@JsonProperty("prdlstNm")
	String productName,

	@JsonProperty("rawmtrl")
	String rawMaterial,

	@JsonProperty("allergy")
	String allergy,

	@JsonProperty("nutrient")
	String nutrient,

	@JsonProperty("barcode")
	String barcode,

	@JsonProperty("prdkind")
	String productKind,

	@JsonProperty("prdkindstate")
	String productKindState,

	@JsonProperty("manufacture")
	String manufacture,

	@JsonProperty("seller")
	String seller,

	@JsonProperty("capacity")
	String capacity,

	@JsonProperty("imgurl1")
	String productImageUrl,

	@JsonProperty("imgurl2")
	String metaImageUrl
) {
}