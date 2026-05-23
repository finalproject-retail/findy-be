package com.retail.map_service.dto.response;

import java.time.OffsetDateTime;

public record BeaconSignalLogResponse(
		Long beaconSignalLogId,
		Long userId,
		Long storeId,
		Long beaconId,
		Long nearestGridId,
		Integer gridX,
		Integer gridY,
		OffsetDateTime timestampIso
) {
}
