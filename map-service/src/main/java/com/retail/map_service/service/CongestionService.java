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
	 * 최근 비콘 신호만 집계합니다. 창이 길면 해산 후에도 혼잡으로 남을 수 있어
	 * 기본 90초로 둡니다. (클라이언트는 구역 변경 시에만 신호를 보냅니다.)
	 */
	private static final int DEFAULT_WINDOW_SECONDS = 90;
	private static final int MIN_WINDOW_SECONDS = 30;
	private static final int MAX_WINDOW_SECONDS = 1_800;

	/*
	 * 매장 전체 인원 기준:
	 * - threshold 이상이면 congested=true
	 * - level은 threshold 대비 비율로 계산합니다.
	 */
	private static final int DEFAULT_STORE_THRESHOLD = 300;

	/*
	 * grid 단위 혼잡도:
	 * - 2명: MEDIUM, 3명 이상: HIGH
	 * - API threshold 파라미터는 HIGH 기준(3)을 덮어쓸 때만 사용합니다.
	 */
	private static final int GRID_CONGESTION_MEDIUM_THRESHOLD = 2;
	private static final int GRID_CONGESTION_HIGH_THRESHOLD = 3;

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
		int resolvedHighThreshold = resolveGridHighThreshold(threshold);

		List<GridCongestionPointResponse> points = beaconSignalLogRepository.findGridCongestionByStore(storeId, from)
			.stream()
			.map(point -> toGridCongestionPoint(point, resolvedHighThreshold))
			.toList();

		return new GridCongestionListResponse(
			storeId,
			resolvedWindowSeconds,
			resolvedHighThreshold,
			points
		);
	}

	private GridCongestionPointResponse toGridCongestionPoint(
		GridCongestionProjection projection,
		int highThreshold
	) {
		long activeUserCount = projection.getActiveUserCount() == null ? 0 : projection.getActiveUserCount();
		CongestionLevel level = resolveGridLevel(activeUserCount, highThreshold);

		return new GridCongestionPointResponse(
			projection.getGridId(),
			projection.getGridX(),
			projection.getGridY(),
			activeUserCount,
			highThreshold,
			level != CongestionLevel.LOW,
			level
		);
	}

	private CongestionLevel resolveGridLevel(long activeUserCount, int highThreshold) {
		if (activeUserCount >= highThreshold) {
			return CongestionLevel.HIGH;
		}

		if (activeUserCount >= GRID_CONGESTION_MEDIUM_THRESHOLD) {
			return CongestionLevel.MEDIUM;
		}

		return CongestionLevel.LOW;
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

	private int resolveGridHighThreshold(Integer threshold) {
		if (threshold != null) {
			return Math.max(GRID_CONGESTION_HIGH_THRESHOLD, resolveThreshold(threshold, GRID_CONGESTION_HIGH_THRESHOLD));
		}

		return GRID_CONGESTION_HIGH_THRESHOLD;
	}

	private void validateStoreExists(Long storeId) {
		if (storeId == null || !storeRepository.existsById(storeId)) {
			throw new BaseException(ErrorCode.STORE_NOT_FOUND);
		}
	}
}
