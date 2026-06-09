package com.retail.map_service.dto.response;

public record StoreResponse(
		Long storeId,
		String storeName,
		String address,
		String status
) {
}
