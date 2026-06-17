package com.princesses7.findy.analytics.zone.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.zone.dto.response.ZoneMovementResponse;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateAnalyticsResponse;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateResponse;
import com.princesses7.findy.analytics.zone.repository.ZoneVisitRateAnalyticsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneVisitRateAnalyticsService {

	private static final int DEFAULT_MIN_STAY_SECONDS = 5;

	private final ZoneVisitRateAnalyticsRepository zoneVisitRateAnalyticsRepository;
	private final AnalyticsPeriodResolver analyticsPeriodResolver;

	public ZoneVisitRateAnalyticsResponse getZoneVisitRates(
		LocalDate startDate,
		LocalDate endDate,
		Long storeId,
		Long zoneId,
		Integer minStaySeconds,
		Boolean includeMovement
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(startDate, endDate);
		int resolvedMinStaySeconds = resolveMinStaySeconds(minStaySeconds);
		boolean resolvedIncludeMovement = Boolean.TRUE.equals(includeMovement);

		List<ZoneVisitRateResponse> rawZoneVisitRates = zoneVisitRateAnalyticsRepository.findZoneVisitRates(
				periodRange,
				storeId,
				zoneId,
				resolvedMinStaySeconds
			)
			.stream()
			.map(ZoneVisitRateResponse::from)
			.toList();

		List<ZoneVisitRateResponse> zoneVisitRates = groupZoneVisitRates(rawZoneVisitRates);

		List<ZoneMovementResponse> rawZoneMovements = resolvedIncludeMovement
			? zoneVisitRateAnalyticsRepository.findZoneMovements(periodRange, storeId, zoneId, resolvedMinStaySeconds)
			  .stream()
			  .map(ZoneMovementResponse::from)
			  .toList()
			: List.of();

		List<ZoneMovementResponse> zoneMovements = resolvedIncludeMovement
			? groupZoneMovements(rawZoneMovements)
			: List.of();

		return new ZoneVisitRateAnalyticsResponse(
			PeriodResponse.from(periodRange),
			storeId,
			zoneId,
			resolvedMinStaySeconds,
			resolvedIncludeMovement,
			zoneVisitRates,
			zoneMovements
		);
	}

	private List<ZoneVisitRateResponse> groupZoneVisitRates(List<ZoneVisitRateResponse> rawZoneVisitRates) {
		Map<AdminZoneGroup, ZoneVisitAggregate> aggregateMap = new EnumMap<>(AdminZoneGroup.class);

		for (ZoneVisitRateResponse item : rawZoneVisitRates) {
			AdminZoneGroup group = AdminZoneGroup.fromZoneId(item.zoneId());
			if (group == null) {
				continue;
			}

			aggregateMap
				.computeIfAbsent(group, ZoneVisitAggregate::new)
				.add(item);
		}

		long totalVisitCount = aggregateMap.values()
			.stream()
			.mapToLong(ZoneVisitAggregate::visitCount)
			.sum();

		List<ZoneVisitAggregate> aggregates = aggregateMap.values()
			.stream()
			.sorted(
				Comparator
					.comparingLong(ZoneVisitAggregate::visitCount)
					.reversed()
					.thenComparing(aggregate -> aggregate.group().sortNo())
			)
			.toList();

		return buildZoneVisitRateResponses(aggregates, totalVisitCount);
	}

	private List<ZoneVisitRateResponse> buildZoneVisitRateResponses(
		List<ZoneVisitAggregate> aggregates,
		long totalVisitCount
	) {
		int rank = 1;

		Map<AdminZoneGroup, Integer> rankByGroup = new EnumMap<>(AdminZoneGroup.class);
		for (ZoneVisitAggregate aggregate : aggregates) {
			rankByGroup.put(aggregate.group(), rank++);
		}

		return aggregateByDefaultOrder(aggregates)
			.stream()
			.map(aggregate -> new ZoneVisitRateResponse(
				aggregate.group().representativeZoneId(),
				aggregate.group().label(),
				aggregate.visitCount(),
				aggregate.uniqueVisitorCount(),
				calculateRate(aggregate.visitCount(), totalVisitCount),
				aggregate.averageStayDurationSeconds(),
				aggregate.totalStayDurationSeconds(),
				rankByGroup.getOrDefault(aggregate.group(), aggregate.group().sortNo())
			))
			.toList();
	}

	private List<ZoneVisitAggregate> aggregateByDefaultOrder(List<ZoneVisitAggregate> aggregates) {
		Map<AdminZoneGroup, ZoneVisitAggregate> byGroup = new EnumMap<>(AdminZoneGroup.class);
		for (ZoneVisitAggregate aggregate : aggregates) {
			byGroup.put(aggregate.group(), aggregate);
		}

		return Arrays.stream(AdminZoneGroup.values())
			.filter(byGroup::containsKey)
			.map(byGroup::get)
			.toList();
	}

	private List<ZoneMovementResponse> groupZoneMovements(List<ZoneMovementResponse> rawZoneMovements) {
		Map<String, ZoneMovementAggregate> aggregateMap = new LinkedHashMap<>();

		for (ZoneMovementResponse item : rawZoneMovements) {
			AdminZoneGroup fromGroup = AdminZoneGroup.fromZoneId(item.fromZoneId());
			AdminZoneGroup toGroup = AdminZoneGroup.fromZoneId(item.toZoneId());

			if (fromGroup == null || toGroup == null) {
				continue;
			}

			String key = fromGroup.name() + "->" + toGroup.name();

			aggregateMap
				.computeIfAbsent(key, ignored -> new ZoneMovementAggregate(fromGroup, toGroup))
				.add(item);
		}

		long totalMovementCount = aggregateMap.values()
			.stream()
			.mapToLong(ZoneMovementAggregate::movementCount)
			.sum();

		return aggregateMap.values()
			.stream()
			.sorted(
				Comparator
					.comparingLong(ZoneMovementAggregate::movementCount)
					.reversed()
					.thenComparing(aggregate -> aggregate.fromGroup().sortNo())
					.thenComparing(aggregate -> aggregate.toGroup().sortNo())
			)
			.map(aggregate -> new ZoneMovementResponse(
				aggregate.fromGroup().representativeZoneId(),
				aggregate.fromGroup().label(),
				aggregate.toGroup().representativeZoneId(),
				aggregate.toGroup().label(),
				aggregate.movementCount(),
				calculateRate(aggregate.movementCount(), totalMovementCount),
				aggregate.averageTravelTimeSeconds()
			))
			.toList();
	}

	private int resolveMinStaySeconds(Integer minStaySeconds) {
		if (minStaySeconds == null) {
			return DEFAULT_MIN_STAY_SECONDS;
		}

		return minStaySeconds;
	}

	private static BigDecimal calculateRate(long numerator, long denominator) {
		if (denominator <= 0) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		return BigDecimal.valueOf(numerator)
			.multiply(BigDecimal.valueOf(100))
			.divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
	}

	private enum AdminZoneGroup {
		FRESH(1, 1L, "신선 식품", 1L, 1L),
		PROCESSED_FROZEN(2, 2L, "가공/냉동 식품", 2L, 2L),
		BAKERY_DELI(3, 3L, "베이커리/델리", 3L, 3L),
		BEVERAGE_ALCOHOL(4, 4L, "음료/주류", 4L, 4L),
		LIFESTYLE(5, 5L, "라이프 스타일", 5L, 5L);

		private final int sortNo;
		private final Long representativeZoneId;
		private final String label;
		private final Long fromZoneId;
		private final Long toZoneId;

		AdminZoneGroup(
			int sortNo,
			Long representativeZoneId,
			String label,
			Long fromZoneId,
			Long toZoneId
		) {
			this.sortNo = sortNo;
			this.representativeZoneId = representativeZoneId;
			this.label = label;
			this.fromZoneId = fromZoneId;
			this.toZoneId = toZoneId;
		}

		static AdminZoneGroup fromZoneId(Long zoneId) {
			if (zoneId == null) {
				return null;
			}

			for (AdminZoneGroup group : values()) {
				if (zoneId >= group.fromZoneId && zoneId <= group.toZoneId) {
					return group;
				}
			}

			return null;
		}

		int sortNo() {
			return sortNo;
		}

		Long representativeZoneId() {
			return representativeZoneId;
		}

		String label() {
			return label;
		}
	}

	private static final class ZoneVisitAggregate {

		private final AdminZoneGroup group;
		private long visitCount;
		private long uniqueVisitorCount;
		private long totalStayDurationSeconds;

		private ZoneVisitAggregate(AdminZoneGroup group) {
			this.group = group;
		}

		private void add(ZoneVisitRateResponse item) {
			visitCount += safeLong(item.visitCount());
			uniqueVisitorCount += safeLong(item.uniqueVisitorCount());
			totalStayDurationSeconds += safeLong(item.totalStayDurationSeconds());
		}

		private AdminZoneGroup group() {
			return group;
		}

		private long visitCount() {
			return visitCount;
		}

		private long uniqueVisitorCount() {
			return uniqueVisitorCount;
		}

		private long totalStayDurationSeconds() {
			return totalStayDurationSeconds;
		}

		private Long averageStayDurationSeconds() {
			if (visitCount <= 0) {
				return 0L;
			}

			return Math.round((double)totalStayDurationSeconds / visitCount);
		}
	}

	private static final class ZoneMovementAggregate {

		private final AdminZoneGroup fromGroup;
		private final AdminZoneGroup toGroup;
		private long movementCount;
		private long weightedTravelTimeSeconds;

		private ZoneMovementAggregate(AdminZoneGroup fromGroup, AdminZoneGroup toGroup) {
			this.fromGroup = fromGroup;
			this.toGroup = toGroup;
		}

		private void add(ZoneMovementResponse item) {
			long itemMovementCount = safeLong(item.movementCount());
			long itemAverageTravelTimeSeconds = safeLong(item.averageTravelTimeSeconds());

			movementCount += itemMovementCount;
			weightedTravelTimeSeconds += itemMovementCount * itemAverageTravelTimeSeconds;
		}

		private AdminZoneGroup fromGroup() {
			return fromGroup;
		}

		private AdminZoneGroup toGroup() {
			return toGroup;
		}

		private long movementCount() {
			return movementCount;
		}

		private Long averageTravelTimeSeconds() {
			if (movementCount <= 0) {
				return 0L;
			}

			return Math.round((double)weightedTravelTimeSeconds / movementCount);
		}
	}

	private static long safeLong(Long value) {
		return value == null ? 0L : value;
	}
}
