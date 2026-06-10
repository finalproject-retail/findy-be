package com.retail.map_service.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PathNavigationRequest(
	@NotNull Long storeId,
	@NotEmpty List<Long> destinationGridIds,
	/** 클라이언트 현재 위치(BLE·기본 출입구). 있으면 DB 비콘 로그 대신 사용 */
	Long currentGridId
) {
}
