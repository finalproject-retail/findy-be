package com.princesses7.findy.shopping.promotion.dto.response;

import java.util.List;

public record PromotionMapMarkerListResponse(
	List<PromotionMapMarkerResponse> markers,
	int markerCount
) {

	public static PromotionMapMarkerListResponse from(List<PromotionMapMarkerResponse> markers) {
		return new PromotionMapMarkerListResponse(
			markers,
			markers.size()
		);
	}
}