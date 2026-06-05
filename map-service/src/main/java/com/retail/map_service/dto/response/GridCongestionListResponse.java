package com.retail.map_service.dto.response;

import java.util.List;

public record GridCongestionListResponse(
	Long storeId,
	int windowSeconds,
	int threshold,
	List<GridCongestionPointResponse> points
) {
}
