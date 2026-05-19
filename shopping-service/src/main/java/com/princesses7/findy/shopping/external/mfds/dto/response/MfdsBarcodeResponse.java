package com.princesses7.findy.shopping.external.mfds.dto.response;

import java.util.List;

public record MfdsBarcodeResponse(
	C005 C005
) {

	public List<MfdsBarcodeItemResponse> getItems() {
		if (C005 == null || C005.row() == null) {
			return List.of();
		}

		return C005.row();
	}

	public record C005(
		String total_count,
		List<MfdsBarcodeItemResponse> row,
		Result RESULT
	) {
	}

	public record Result(
		String MSG,
		String CODE
	) {
	}
}