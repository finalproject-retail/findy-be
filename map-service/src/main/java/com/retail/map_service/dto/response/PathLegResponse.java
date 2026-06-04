package com.retail.map_service.dto.response;

import java.util.List;

public record PathLegResponse(
	Long fromGridId,
	Long toGridId,
	List<Long> pathGridIds
) {
}
