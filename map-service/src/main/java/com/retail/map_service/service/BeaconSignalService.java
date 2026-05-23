package com.retail.map_service.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retail.map_service.dto.request.BeaconSignalCreateRequest;
import com.retail.map_service.dto.response.BeaconSignalLogResponse;
import com.retail.map_service.entity.BeaconEntity;
import com.retail.map_service.entity.BeaconSignalLogEntity;
import com.retail.map_service.entity.GridEntity;
import com.retail.map_service.entity.StoreEntity;
import com.retail.map_service.global.exception.BaseException;
import com.retail.map_service.global.exception.ErrorCode;
import com.retail.map_service.repository.BeaconRepository;
import com.retail.map_service.repository.BeaconSignalLogRepository;
import com.retail.map_service.repository.GridRepository;
import com.retail.map_service.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BeaconSignalService {

	private static final ZoneOffset KST = ZoneOffset.ofHours(9);

	private final StoreRepository storeRepository;
	private final GridRepository gridRepository;
	private final BeaconRepository beaconRepository;
	private final BeaconSignalLogRepository beaconSignalLogRepository;

	@Transactional
	public BeaconSignalLogResponse recordGridChange(Long userId, BeaconSignalCreateRequest request) {
		StoreEntity store = storeRepository.findById(request.storeId())
				.orElseThrow(() -> new BaseException(ErrorCode.STORE_NOT_FOUND));

		GridEntity nearestGrid = gridRepository.findByGridIdAndStore_StoreId(request.nearestGridId(), request.storeId())
				.orElseThrow(() -> new BaseException(ErrorCode.GRID_NOT_FOUND));

		BeaconEntity beacon = beaconRepository.findByStore_StoreIdAndGrid_GridId(request.storeId(), request.nearestGridId())
				.orElseThrow(() -> new BaseException(ErrorCode.BEACON_NOT_FOUND));

		OffsetDateTime timestampKst = toKstOffset(request.timestampIso());

		BeaconSignalLogEntity log = BeaconSignalLogEntity.builder()
				.userId(userId)
				.store(store)
				.beacon(beacon)
				.timestampIso(timestampKst)
				.mac(normalizeMac(request.mac()))
				.bluetoothAddressHex(normalizeHex(request.bluetoothAddressHex()))
				.rssi(request.rssi())
				.uuid(request.uuid())
				.major(request.major())
				.minor(request.minor())
				.tx(request.tx())
				.nearestGrid(nearestGrid)
				.build();

		BeaconSignalLogEntity saved = beaconSignalLogRepository.save(log);

		return new BeaconSignalLogResponse(
				saved.getBeaconSignalLogId(),
				saved.getUserId(),
				store.getStoreId(),
				beacon.getBeaconId(),
				nearestGrid.getGridId(),
				nearestGrid.getGridX(),
				nearestGrid.getGridY(),
				saved.getTimestampIso()
		);
	}

	private static OffsetDateTime toKstOffset(OffsetDateTime value) {
		if (value == null) {
			return null;
		}
		return value.withOffsetSameInstant(KST);
	}

	private String normalizeMac(String mac) {
		if (mac == null || mac.isBlank()) {
			return null;
		}
		return mac.trim();
	}

	private String normalizeHex(String hex) {
		if (hex == null || hex.isBlank()) {
			return null;
		}
		String value = hex.trim();
		if (value.startsWith("0x") || value.startsWith("0X")) {
			return value.substring(2);
		}
		return value;
	}
}
