package com.retail.map_service.dto.response;

public record GridCongestionPointResponse(
	Long gridId,
	Integer gridX,
	Integer gridY,
	long activeUserCount,
	int threshold,
	boolean congested,
	CongestionLevel level
) {
}
