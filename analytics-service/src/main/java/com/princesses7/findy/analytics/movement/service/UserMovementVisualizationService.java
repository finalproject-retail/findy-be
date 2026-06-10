package com.princesses7.findy.analytics.movement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.analytics.analytics.dto.response.PeriodResponse;
import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementFlowQueryResult;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementHeatmapQueryResult;
import com.princesses7.findy.analytics.movement.dto.query.UserMovementPointQueryResult;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementFlowResponse;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementHeatmapResponse;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementPathResponse;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementPointResponse;
import com.princesses7.findy.analytics.movement.dto.response.UserMovementVisualizationResponse;
import com.princesses7.findy.analytics.movement.repository.UserMovementVisualizationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMovementVisualizationService {

	private static final int DEFAULT_LIMIT = 100;
	private static final int DEFAULT_MIN_STAY_SECONDS = 0;

	private final AnalyticsPeriodResolver analyticsPeriodResolver;
	private final UserMovementVisualizationRepository userMovementVisualizationRepository;

	public UserMovementVisualizationResponse getUserMovementVisualization(
		LocalDate fromDate,
		LocalDate toDate,
		Long storeId,
		Long userId,
		Integer minStaySeconds,
		Integer limit
	) {
		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);
		int resolvedLimit = resolveLimit(limit);
		int resolvedMinStaySeconds = resolveMinStaySeconds(minStaySeconds);

		List<UserMovementPointQueryResult> movementPoints = userMovementVisualizationRepository.findMovementPoints(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds,
			resolvedLimit
		);

		List<UserMovementHeatmapQueryResult> heatmaps = userMovementVisualizationRepository.findHeatmaps(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds,
			resolvedLimit
		);

		List<UserMovementFlowQueryResult> flows = userMovementVisualizationRepository.findFlows(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds,
			resolvedLimit
		);

		Long totalUserCount = userMovementVisualizationRepository.countTotalUsers(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds
		);

		Long totalVisitCount = userMovementVisualizationRepository.countTotalVisits(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds
		);

		Long totalStayDurationSeconds = userMovementVisualizationRepository.sumTotalStayDuration(
			periodRange,
			storeId,
			userId,
			resolvedMinStaySeconds
		);

		return new UserMovementVisualizationResponse(
			PeriodResponse.from(periodRange),
			storeId,
			userId,
			resolvedMinStaySeconds,
			resolvedLimit,
			totalUserCount,
			totalVisitCount,
			totalStayDurationSeconds,
			toPathResponses(movementPoints),
			heatmaps.stream()
				.map(UserMovementHeatmapResponse::from)
				.toList(),
			flows.stream()
				.map(UserMovementFlowResponse::from)
				.toList()
		);
	}

	private List<UserMovementPathResponse> toPathResponses(List<UserMovementPointQueryResult> movementPoints) {
		Map<Long, List<UserMovementPointQueryResult>> pointsByUser = movementPoints.stream()
			.collect(Collectors.groupingBy(UserMovementPointQueryResult::userId));

		List<UserMovementPathResponse> responses = new ArrayList<>();

		for (Map.Entry<Long, List<UserMovementPointQueryResult>> entry : pointsByUser.entrySet()) {
			List<UserMovementPointQueryResult> userPoints = entry.getValue().stream()
				.sorted((left, right) -> {
					int compared = left.enteredAt().compareTo(right.enteredAt());
					if (compared != 0) {
						return compared;
					}
					return left.locationLogId().compareTo(right.locationLogId());
				})
				.toList();

			Long totalStayDurationSeconds = userPoints.stream()
				.map(UserMovementPointQueryResult::stayDurationSeconds)
				.reduce(0L, Long::sum);

			LocalDateTime firstEnteredAt = userPoints.isEmpty() ? null : userPoints.get(0).enteredAt();
			LocalDateTime lastExitedAt = userPoints.isEmpty()
				? null
				: userPoints.get(userPoints.size() - 1).exitedAt();

			responses.add(new UserMovementPathResponse(
				entry.getKey(),
				userPoints.size(),
				totalStayDurationSeconds,
				firstEnteredAt,
				lastExitedAt,
				userPoints.stream()
					.map(UserMovementPointResponse::from)
					.toList()
			));
		}

		return responses.stream()
			.sorted((left, right) -> Long.compare(left.userId(), right.userId()))
			.toList();
	}

	private int resolveLimit(Integer limit) {
		if (limit == null) {
			return DEFAULT_LIMIT;
		}
		return limit;
	}

	private int resolveMinStaySeconds(Integer minStaySeconds) {
		if (minStaySeconds == null) {
			return DEFAULT_MIN_STAY_SECONDS;
		}
		return minStaySeconds;
	}
}