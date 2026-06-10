package com.retail.map_service.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retail.map_service.domain.path.AislePathfinder;
import com.retail.map_service.domain.path.ShoppingVisitOrderOptimizer;
import com.retail.map_service.domain.path.StoreGridMap;
import com.retail.map_service.dto.request.PathNavigationRequest;
import com.retail.map_service.dto.response.PathLegResponse;
import com.retail.map_service.dto.response.PathNavigationResponse;
import com.retail.map_service.global.exception.BaseException;
import com.retail.map_service.global.exception.ErrorCode;
import com.retail.map_service.entity.BeaconSignalLogEntity;
import com.retail.map_service.entity.GridCellType;
import com.retail.map_service.entity.GridEntity;
import com.retail.map_service.repository.BeaconSignalLogRepository;
import com.retail.map_service.repository.GridRepository;
import com.retail.map_service.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorePathService {

	private final StoreRepository storeRepository;
	private final GridRepository gridRepository;
	private final BeaconSignalLogRepository beaconSignalLogRepository;

	@Transactional(readOnly = true)
	public PathNavigationResponse navigate(Long userId, PathNavigationRequest request) {
		Long storeId = request.storeId();
		validateStoreExists(storeId);

		StoreGridMap storeGridMap = new StoreGridMap(
			gridRepository.findByStore_StoreIdOrderByGridIdAsc(storeId)
		);

		Long currentGridId = resolveCurrentGridId(
			userId,
			storeId,
			storeGridMap,
			request.currentGridId()
		);
		List<Long> destinationGridIds = dedupePreserveOrder(request.destinationGridIds());
		destinationGridIds.forEach(gridId -> validateGridExists(storeGridMap, gridId, storeId));
		destinationGridIds.forEach(gridId -> {
			if (AislePathfinder.resolveWalkableGridId(gridId, storeGridMap) == null) {
				throw new BaseException(
					ErrorCode.PATH_DESTINATION_NOT_NAVIGABLE,
					"경로를 찾을 수 없는 목적지 격자입니다. gridId=" + gridId
				);
			}
		});

		Long cursorGridId = AislePathfinder.resolveWalkableGridId(currentGridId, storeGridMap);
		if (cursorGridId == null) {
			throw new BaseException(ErrorCode.PATH_CURRENT_NOT_NAVIGABLE);
		}

		List<Long> orderedDestinationGridIds = ShoppingVisitOrderOptimizer.orderMinimumVisit(
			cursorGridId,
			destinationGridIds,
			storeGridMap
		);

		List<PathLegResponse> legs = new ArrayList<>();
		List<Long> fullPathGridIds = new ArrayList<>();

		for (Long destinationGridId : orderedDestinationGridIds) {
			Long goalGridId = AislePathfinder.resolveWalkableGridId(destinationGridId, storeGridMap);
			if (goalGridId == null) {
				throw new BaseException(
					ErrorCode.PATH_DESTINATION_NOT_NAVIGABLE,
					"경로를 찾을 수 없는 목적지 격자입니다. gridId=" + destinationGridId
				);
			}

			if (cursorGridId.equals(goalGridId)) {
				continue;
			}

			List<Long> legGridIds = AislePathfinder.findPathGridIds(
				cursorGridId,
				goalGridId,
				storeGridMap,
				fullPathGridIds
			);
			if (legGridIds.size() < 2) {
				throw new BaseException(
					ErrorCode.PATH_NOT_FOUND,
					"목적지까지 통로 경로를 찾을 수 없습니다. gridId=" + destinationGridId
				);
			}

			legs.add(new PathLegResponse(
				legGridIds.get(0),
				legGridIds.get(legGridIds.size() - 1),
				legGridIds
			));
			appendFullPath(fullPathGridIds, legGridIds);
			cursorGridId = goalGridId;
		}

		if (legs.isEmpty()) {
			throw new BaseException(ErrorCode.PATH_NOT_FOUND, "목적지까지 통로 경로를 찾을 수 없습니다.");
		}

		return new PathNavigationResponse(
			storeId,
			currentGridId,
			destinationGridIds,
			legs,
			fullPathGridIds
		);
	}

	/**
	 * 1) 요청 {@code currentGridId}, 2) 최신 비콘, 3) 비콘 없으면 매장 {@code START} 격자.
	 */
	private Long resolveCurrentGridId(
		Long userId,
		Long storeId,
		StoreGridMap storeGridMap,
		Long requestedCurrentGridId
	) {
		if (requestedCurrentGridId != null) {
			validateGridExists(storeGridMap, requestedCurrentGridId, storeId);
			return requestedCurrentGridId;
		}

		Long fromBeacon = beaconSignalLogRepository
			.findTopByUserIdAndStore_StoreIdOrderByTimestampIsoDesc(userId, storeId)
			.map(BeaconSignalLogEntity::getNearestGrid)
			.map(GridEntity::getGridId)
			.orElse(null);

		if (fromBeacon != null) {
			validateGridExists(storeGridMap, fromBeacon, storeId);
			return fromBeacon;
		}

		Long startGridId = gridRepository
			.findFirstByStore_StoreIdAndCellTypeOrderByGridIdAsc(storeId, GridCellType.START)
			.map(GridEntity::getGridId)
			.orElseThrow(() -> new BaseException(ErrorCode.PATH_START_GRID_NOT_FOUND));

		validateGridExists(storeGridMap, startGridId, storeId);
		return startGridId;
	}

	private void validateStoreExists(Long storeId) {
		if (!storeRepository.existsById(storeId)) {
			throw new BaseException(ErrorCode.STORE_NOT_FOUND);
		}
	}

	private void validateGridExists(StoreGridMap storeGridMap, Long gridId, Long storeId) {
		if (gridRepository.findByGridIdAndStore_StoreId(gridId, storeId).isEmpty()) {
			if (storeGridMap.getGrid(gridId) != null) {
				throw new BaseException(
					ErrorCode.GRID_NOT_FOUND,
					"해당 매장의 격자가 아닙니다. gridId=" + gridId
				);
			}
			throw new BaseException(ErrorCode.GRID_NOT_FOUND, "격자를 찾을 수 없습니다. gridId=" + gridId);
		}
	}

	private List<Long> dedupePreserveOrder(List<Long> destinationGridIds) {
		List<Long> result = new ArrayList<>();
		Long previous = null;
		for (Long gridId : destinationGridIds) {
			if (gridId == null) {
				continue;
			}
			if (gridId.equals(previous)) {
				continue;
			}
			result.add(gridId);
			previous = gridId;
		}

		if (result.isEmpty()) {
			throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "destinationGridIds는 비어 있을 수 없습니다.");
		}

		return result;
	}

	private void appendFullPath(List<Long> fullPathGridIds, List<Long> legGridIds) {
		if (legGridIds.isEmpty()) {
			return;
		}

		if (fullPathGridIds.isEmpty()) {
			fullPathGridIds.addAll(legGridIds);
			return;
		}

		Long lastGridId = fullPathGridIds.get(fullPathGridIds.size() - 1);
		int startIndex = legGridIds.get(0).equals(lastGridId) ? 1 : 0;
		for (int i = startIndex; i < legGridIds.size(); i++) {
			fullPathGridIds.add(legGridIds.get(i));
		}
	}
}
