package com.princesses7.findy.shopping.external.mfds.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsBarcodeResponse(

	@JsonProperty("I2570")
	MfdsBarcodeBody body
) {

	public List<MfdsBarcodeItemResponse> getItems() {
		if (body == null || body.row() == null) {
			return List.of();
		}

		return body.row();
	}
}