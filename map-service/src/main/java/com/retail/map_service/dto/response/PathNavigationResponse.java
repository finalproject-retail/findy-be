package com.retail.map_service.dto.response;

import java.util.List;

public record PathNavigationResponse(
	Long storeId,
	Long currentGridId,
	List<Long> destinationGridIds,
	List<PathLegResponse> legs,
	List<Long> fullPathGridIds
) {
}
