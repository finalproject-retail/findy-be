package com.retail.map_service.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.retail.map_service.dto.response.CongestionLevel;
import com.retail.map_service.dto.response.GridCongestionListResponse;
import com.retail.map_service.dto.response.GridCongestionPointResponse;
import com.retail.map_service.dto.response.StoreCongestionResponse;
import com.retail.map_service.global.exception.BaseException;
import com.retail.map_service.global.exception.ErrorCode;
import com.retail.map_service.repository.BeaconSignalLogRepository;
import com.retail.map_service.repository.StoreRepository;
import com.retail.map_service.repository.projection.GridCongestionProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CongestionService {

	private static final ZoneOffset KST = ZoneOffset.ofHours(9);

	/*
	 * 혼잡도 기준은 초기 임의값입니다.
	 * 운영 데이터가 쌓이면 매장 크기, 시간대, BLE 전송 주기 기준으로 조정하세요.
	 */
	private static final int DEFAULT_WINDOW_SECONDS = 300;
	private static final int MIN_WINDOW_SECONDS = 30;
	private static final int MAX_WINDOW_SECONDS = 1_800;

	/*
	 * 매장 전체 인원 기준:
	 * - threshold 이상이면 congested=true
	 * - level은 threshold 대비 비율로 계산합니다.
	 */
	private static final int DEFAULT_STORE_THRESHOLD = 300;

	/*
	 * grid 단위 인원 기준:
	 * - 기본값은 매장 전체 활성 인원의 일정 비율입니다. 현재 10프로
	 * - threshold 이상이면 congested=true
	 */
	private static final double DEFAULT_GRID_CONGESTION_RATIO = 0.1;
	private static final int MIN_GRID_CONGESTION_THRESHOLD = 1;

	private final StoreRepository storeRepository;
	private final BeaconSignalLogRepository beaconSignalLogRepository;

	@Transactional(readOnly = true)
	public StoreCongestionResponse getStoreCongestion(
		Long storeId,
		Integer windowSeconds,
		Integer threshold
	) {
		validateStoreExists(storeId);

		int resolvedWindowSeconds = resolveWindowSeconds(windowSeconds);
		int resolvedThreshold = resolveThreshold(threshold, DEFAULT_STORE_THRESHOLD);
		OffsetDateTime from = OffsetDateTime.now(KST).minusSeconds(resolvedWindowSeconds);

		long activeUserCount = beaconSignalLogRepository.countActiveUsersByStore(storeId, from);

		return new StoreCongestionResponse(
			storeId,
			resolvedWindowSeconds,
			activeUserCount,
			resolvedThreshold,
			activeUserCount >= resolvedThreshold,
			resolveLevel(activeUserCount, resolvedThreshold)
		);
	}

	@Transactional(readOnly = true)
	public GridCongestionListResponse getGridCongestion(
		Long storeId,
		Integer windowSeconds,
		Integer threshold
	) {
		validateStoreExists(storeId);

		int resolvedWindowSeconds = resolveWindowSeconds(windowSeconds);
		OffsetDateTime from = OffsetDateTime.now(KST).minusSeconds(resolvedWindowSeconds);
		long storeActiveUserCount = beaconSignalLogRepository.countActiveUsersByStore(storeId, from);
		int resolvedThreshold = resolveGridThreshold(threshold, storeActiveUserCount);

		List<GridCongestionPointResponse> points = beaconSignalLogRepository.findGridCongestionByStore(storeId, from)
			.stream()
			.map(point -> toGridCongestionPoint(point, resolvedThreshold))
			.toList();

		return new GridCongestionListResponse(
			storeId,
			resolvedWindowSeconds,
			resolvedThreshold,
			points
		);
	}

	private GridCongestionPointResponse toGridCongestionPoint(
		GridCongestionProjection projection,
		int threshold
	) {
		long activeUserCount = projection.getActiveUserCount() == null ? 0 : projection.getActiveUserCount();

		return new GridCongestionPointResponse(
			projection.getGridId(),
			projection.getGridX(),
			projection.getGridY(),
			activeUserCount,
			threshold,
			activeUserCount >= threshold,
			resolveLevel(activeUserCount, threshold)
		);
	}

	private CongestionLevel resolveLevel(
		long activeUserCount,
		int threshold
	) {
		if (activeUserCount >= threshold) {
			return CongestionLevel.HIGH;
		}

		if (activeUserCount >= Math.max(1, threshold / 2)) {
			return CongestionLevel.MEDIUM;
		}

		return CongestionLevel.LOW;
	}

	private int resolveWindowSeconds(Integer windowSeconds) {
		if (windowSeconds == null) {
			return DEFAULT_WINDOW_SECONDS;
		}

		if (windowSeconds < MIN_WINDOW_SECONDS || windowSeconds > MAX_WINDOW_SECONDS) {
			throw new BaseException(
				ErrorCode.INVALID_INPUT_VALUE,
				"windowSeconds는 " + MIN_WINDOW_SECONDS + "초 이상 " + MAX_WINDOW_SECONDS + "초 이하여야 합니다."
			);
		}

		return windowSeconds;
	}

	private int resolveThreshold(
		Integer threshold,
		int defaultThreshold
	) {
		if (threshold == null) {
			return defaultThreshold;
		}

		if (threshold <= 0) {
			throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "threshold는 1 이상이어야 합니다.");
		}

		return threshold;
	}

	private int resolveGridThreshold(
		Integer threshold,
		long storeActiveUserCount
	) {
		if (threshold != null) {
			return Math.max(MIN_GRID_CONGESTION_THRESHOLD, resolveThreshold(threshold, 1));
		}

		return Math.max(
			MIN_GRID_CONGESTION_THRESHOLD,
			(int)Math.ceil(storeActiveUserCount * DEFAULT_GRID_CONGESTION_RATIO)
		);
	}

	private void validateStoreExists(Long storeId) {
		if (storeId == null || !storeRepository.existsById(storeId)) {
			throw new BaseException(ErrorCode.STORE_NOT_FOUND);
		}
	}
}
