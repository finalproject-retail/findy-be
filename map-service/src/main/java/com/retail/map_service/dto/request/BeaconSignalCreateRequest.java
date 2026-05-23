package com.retail.map_service.dto.request;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BeaconSignalCreateRequest(
		@NotNull Long storeId,
		@NotNull Long nearestGridId,
		@NotNull OffsetDateTime timestampIso,
		@NotBlank String uuid,
		Integer major,
		Integer minor,
		Integer rssi,
		String mac,
		String bluetoothAddressHex,
		Integer tx
) {
}
