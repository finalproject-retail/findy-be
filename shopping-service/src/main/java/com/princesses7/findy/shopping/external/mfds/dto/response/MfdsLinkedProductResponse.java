package com.princesses7.findy.shopping.external.mfds.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MfdsLinkedProductResponse(

	@JsonProperty("C005")
	MfdsLinkedProductBody body
) {

	public List<MfdsLinkedProductItemResponse> getItems() {
		if (body == null || body.row() == null) {
			return List.of();
		}

		return body.row();
	}

	public MfdsLinkedProductItemResponse getFirstItemOrNull() {
		List<MfdsLinkedProductItemResponse> items = getItems();

		if (items.isEmpty()) {
			return null;
		}

		return items.get(0);
	}
}