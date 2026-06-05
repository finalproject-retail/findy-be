package com.retail.map_service.dto.response;

public record StoreCongestionResponse(
	Long storeId,
	int windowSeconds,
	long activeUserCount,
	int threshold,
	boolean congested,
	CongestionLevel level
) {
}
