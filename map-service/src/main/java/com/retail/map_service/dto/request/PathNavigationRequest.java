package com.retail.map_service.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PathNavigationRequest(
	@NotNull Long storeId,
	@NotEmpty List<Long> destinationGridIds
) {
}
