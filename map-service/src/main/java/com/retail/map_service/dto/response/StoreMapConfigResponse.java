package com.retail.map_service.dto.response;

import java.util.List;

public record StoreMapConfigResponse(
		StoreSummary store,
		MapSummary map,
		LayoutSummary layout,
		List<GridItem> grids,
		List<BeaconItem> beacons
) {
	public record StoreSummary(
			Long storeId,
			String storeName,
			String address,
			String status
	) {
	}

	public record MapSummary(
			Long mapId,
			String mapImageUrl,
			Integer widthMeters,
			Integer heightMeters
	) {
	}

	public record LayoutSummary(
			int gridCols,
			int gridRows,
			int cellSizeMeters,
			String gridIdFormula
	) {
	}

	public record GridItem(
			Long gridId,
			int gridX,
			int gridY,
			String cellType
	) {
	}

	public record BeaconItem(
			Long beaconId,
			Long gridId,
			int gridX,
			int gridY,
			String beaconUuid,
			Integer major,
			Integer minor,
			String mac
	) {
	}
}
