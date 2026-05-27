package com.retail.map_service.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retail.map_service.domain.StoreMapLayout;
import com.retail.map_service.dto.response.StoreMapConfigResponse;
import com.retail.map_service.dto.response.StoreMapConfigResponse.BeaconItem;
import com.retail.map_service.dto.response.StoreMapConfigResponse.GridItem;
import com.retail.map_service.dto.response.StoreMapConfigResponse.LayoutSummary;
import com.retail.map_service.dto.response.StoreMapConfigResponse.MapSummary;
import com.retail.map_service.dto.response.StoreMapConfigResponse.StoreSummary;
import com.retail.map_service.entity.BeaconEntity;
import com.retail.map_service.entity.GridEntity;
import com.retail.map_service.entity.StoreEntity;
import com.retail.map_service.entity.StoreMapEntity;
import com.retail.map_service.global.exception.BaseException;
import com.retail.map_service.global.exception.ErrorCode;
import com.retail.map_service.repository.BeaconRepository;
import com.retail.map_service.repository.GridRepository;
import com.retail.map_service.repository.StoreMapRepository;
import com.retail.map_service.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreMapConfigService {

	private final StoreRepository storeRepository;
	private final StoreMapRepository storeMapRepository;
	private final GridRepository gridRepository;
	private final BeaconRepository beaconRepository;

	@Transactional(readOnly = true)
	public StoreMapConfigResponse getMapConfig(Long storeId) {
		StoreEntity store = storeRepository.findById(storeId)
				.orElseThrow(() -> new BaseException(ErrorCode.STORE_NOT_FOUND));

		StoreMapEntity storeMap = storeMapRepository.findFirstByStore_StoreIdOrderByMapIdAsc(storeId)
				.orElseThrow(() -> new BaseException(ErrorCode.STORE_MAP_NOT_FOUND));

		List<GridEntity> grids = gridRepository.findByStore_StoreIdOrderByGridIdAsc(storeId);
		List<BeaconEntity> beacons = beaconRepository.findAllByStoreIdWithGrid(storeId);

		return new StoreMapConfigResponse(
				toStoreSummary(store),
				toMapSummary(storeMap),
				toLayoutSummary(),
				grids.stream().map(this::toGridItem).toList(),
				beacons.stream().map(this::toBeaconItem).toList()
		);
	}

	private StoreSummary toStoreSummary(StoreEntity store) {
		return new StoreSummary(
				store.getStoreId(),
				store.getStoreName(),
				store.getAddress(),
				store.getStatus().name()
		);
	}

	private MapSummary toMapSummary(StoreMapEntity storeMap) {
		return new MapSummary(
				storeMap.getMapId(),
				storeMap.getMapImageUrl(),
				storeMap.getWidth(),
				storeMap.getHeight()
		);
	}

	private LayoutSummary toLayoutSummary() {
		return new LayoutSummary(
				StoreMapLayout.GRID_COLS,
				StoreMapLayout.GRID_ROWS,
				StoreMapLayout.CELL_SIZE_METERS,
				StoreMapLayout.GRID_ID_FORMULA
		);
	}

	private GridItem toGridItem(GridEntity grid) {
		return new GridItem(
				grid.getGridId(),
				grid.getGridX(),
				grid.getGridY(),
				grid.getCellType().name()
		);
	}

	private BeaconItem toBeaconItem(BeaconEntity beacon) {
		var grid = beacon.getGrid();
		return new BeaconItem(
				beacon.getBeaconId(),
				grid.getGridId(),
				grid.getGridX(),
				grid.getGridY(),
				beacon.getBeaconUuid(),
				beacon.getMajor(),
				beacon.getMinor(),
				beacon.getMac()
		);
	}
}
