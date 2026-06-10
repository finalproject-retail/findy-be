package com.retail.map_service.domain.path;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.retail.map_service.entity.GridEntity;

/**
 * {@code grids.cell_type}이 통행 가능한 격자만 이용한 경로 탐색. 외부 계약은 {@code grid_id} 기준.
 */
public final class AislePathfinder {

	private static final int[][] NEIGHBORS = {
		{ 1, 0 },
		{ -1, 0 },
		{ 0, 1 },
		{ 0, -1 }
	};

	private AislePathfinder() {
	}

	/**
	 * 격자 {@code gridId}에서 실제로 걸어갈 통로 격자의 {@code grid_id}.
	 * 이미 START/AISLE이면 그대로, 아니면 가장 가까운 통행 가능 격자로 스냅.
	 */
	public static Long resolveWalkableGridId(Long gridId, StoreGridMap storeGridMap) {
		if (storeGridMap.isWalkable(gridId)) {
			return gridId;
		}

		GridEntity grid = storeGridMap.getGrid(gridId);
		if (grid == null) {
			return null;
		}

		return findNearestWalkableGridId(grid.getGridX(), grid.getGridY(), storeGridMap);
	}

	/**
	 * 통행 가능 격자 {@code fromGridId} → {@code toGridId} 최단 경로 ({@code grid_id} 목록).
	 */
	public static List<Long> findPathGridIds(
		Long fromGridId,
		Long toGridId,
		StoreGridMap storeGridMap
	) {
		return findPathGridIds(fromGridId, toGridId, storeGridMap, List.of());
	}

	/**
	 * 최단 경로를 찾되, 동일 칸 수일 때 {@code priorPathGridIds}와 겹침·즉시 되돌아감을 최소화.
	 */
	public static List<Long> findPathGridIds(
		Long fromGridId,
		Long toGridId,
		StoreGridMap storeGridMap,
		List<Long> priorPathGridIds
	) {
		if (!storeGridMap.isWalkable(fromGridId) || !storeGridMap.isWalkable(toGridId)) {
			return List.of();
		}

		if (priorPathGridIds == null || priorPathGridIds.isEmpty()) {
			return bfsPathByGridId(fromGridId, toGridId, storeGridMap);
		}

		Map<Long, Integer> distances = bfsDistancesByGridId(fromGridId, toGridId, storeGridMap);
		if (!distances.containsKey(toGridId)) {
			return List.of();
		}

		Set<Long> priorPathSet = new HashSet<>(priorPathGridIds);
		Long immediateBacktrackGridId = resolveImmediateBacktrackGridId(fromGridId, priorPathGridIds);
		List<Long> path = reconstructShortestPathWithTieBreak(
			fromGridId,
			toGridId,
			storeGridMap,
			distances,
			priorPathSet,
			immediateBacktrackGridId
		);
		if (!path.isEmpty()) {
			return path;
		}

		return bfsPathByGridId(fromGridId, toGridId, storeGridMap);
	}

	/** 통행 가능 격자 간 BFS 최단 통로 칸 수. 도달 불가 시 {@link Integer#MAX_VALUE}. */
	public static int pathStepCount(Long fromGridId, Long toGridId, StoreGridMap storeGridMap) {
		if (fromGridId.equals(toGridId)) {
			return 0;
		}
		if (!storeGridMap.isWalkable(fromGridId) || !storeGridMap.isWalkable(toGridId)) {
			return Integer.MAX_VALUE;
		}

		List<Long> path = bfsPathByGridId(fromGridId, toGridId, storeGridMap);
		if (path.size() < 2) {
			return Integer.MAX_VALUE;
		}
		return path.size() - 1;
	}

	private static Long findNearestWalkableGridId(
		int gridX,
		int gridY,
		StoreGridMap storeGridMap
	) {
		Long startGridId = storeGridMap.gridIdAt(gridX, gridY);
		if (startGridId == null) {
			return null;
		}

		Set<Long> visited = new HashSet<>();
		visited.add(startGridId);
		Queue<Long> queue = new ArrayDeque<>();
		queue.add(startGridId);

		while (!queue.isEmpty()) {
			Long currentGridId = queue.poll();
			if (storeGridMap.isWalkable(currentGridId)) {
				return currentGridId;
			}

			for (Long neighborGridId : neighborGridIds(currentGridId, storeGridMap)) {
				if (visited.add(neighborGridId)) {
					queue.add(neighborGridId);
				}
			}
		}

		return null;
	}

	private static Long resolveImmediateBacktrackGridId(Long fromGridId, List<Long> priorPathGridIds) {
		if (priorPathGridIds.size() < 2) {
			return null;
		}

		Long lastGridId = priorPathGridIds.get(priorPathGridIds.size() - 1);
		if (!fromGridId.equals(lastGridId)) {
			return null;
		}

		return priorPathGridIds.get(priorPathGridIds.size() - 2);
	}

	private static Map<Long, Integer> bfsDistancesByGridId(
		Long fromGridId,
		Long toGridId,
		StoreGridMap storeGridMap
	) {
		Map<Long, Integer> distances = new HashMap<>();
		Set<Long> visited = new HashSet<>();
		visited.add(fromGridId);
		Queue<Long> queue = new ArrayDeque<>();
		queue.add(fromGridId);
		distances.put(fromGridId, 0);

		while (!queue.isEmpty()) {
			Long currentGridId = queue.poll();
			if (currentGridId.equals(toGridId)) {
				break;
			}

			int nextDistance = distances.get(currentGridId) + 1;
			for (Long neighborGridId : neighborGridIds(currentGridId, storeGridMap)) {
				if (!storeGridMap.isWalkable(neighborGridId) || !visited.add(neighborGridId)) {
					continue;
				}

				distances.put(neighborGridId, nextDistance);
				queue.add(neighborGridId);
			}
		}

		return distances;
	}

	private static List<Long> reconstructShortestPathWithTieBreak(
		Long fromGridId,
		Long toGridId,
		StoreGridMap storeGridMap,
		Map<Long, Integer> distances,
		Set<Long> priorPathSet,
		Long immediateBacktrackGridId
	) {
		List<Long> path = new ArrayList<>();
		path.add(fromGridId);
		if (searchShortestPathWithTieBreak(
			fromGridId,
			toGridId,
			storeGridMap,
			distances,
			priorPathSet,
			immediateBacktrackGridId,
			path
		)) {
			return path;
		}

		return List.of();
	}

	/** 최단거리 DAG 위에서 tie-break 후보를 순서대로 시도한다 (greedy 단일 선택은 막다른 길 가능). */
	private static boolean searchShortestPathWithTieBreak(
		Long currentGridId,
		Long toGridId,
		StoreGridMap storeGridMap,
		Map<Long, Integer> distances,
		Set<Long> priorPathSet,
		Long immediateBacktrackGridId,
		List<Long> path
	) {
		if (currentGridId.equals(toGridId)) {
			return true;
		}

		int nextDistance = distances.get(currentGridId) + 1;
		List<Long> candidates = new ArrayList<>();
		List<Integer> scores = new ArrayList<>();

		for (Long neighborGridId : neighborGridIds(currentGridId, storeGridMap)) {
			if (!storeGridMap.isWalkable(neighborGridId)) {
				continue;
			}

			Integer neighborDistance = distances.get(neighborGridId);
			if (neighborDistance == null || neighborDistance != nextDistance) {
				continue;
			}

			candidates.add(neighborGridId);
			scores.add(tieBreakScore(neighborGridId, priorPathSet, immediateBacktrackGridId));
		}

		if (candidates.isEmpty()) {
			return false;
		}

		Integer[] candidateOrder = new Integer[candidates.size()];
		for (int i = 0; i < candidateOrder.length; i++) {
			candidateOrder[i] = i;
		}
		Arrays.sort(candidateOrder, Comparator.comparingInt(scores::get));

		for (int candidateIndex : candidateOrder) {
			Long neighborGridId = candidates.get(candidateIndex);
			path.add(neighborGridId);
			if (searchShortestPathWithTieBreak(
				neighborGridId,
				toGridId,
				storeGridMap,
				distances,
				priorPathSet,
				immediateBacktrackGridId,
				path
			)) {
				return true;
			}
			path.remove(path.size() - 1);
		}

		return false;
	}

	private static int tieBreakScore(
		Long neighborGridId,
		Set<Long> priorPathSet,
		Long immediateBacktrackGridId
	) {
		int score = 0;
		if (priorPathSet.contains(neighborGridId)) {
			score += 1_000;
		}
		if (neighborGridId.equals(immediateBacktrackGridId)) {
			score += 10_000;
		}
		return score;
	}

	private static List<Long> bfsPathByGridId(
		Long fromGridId,
		Long toGridId,
		StoreGridMap storeGridMap
	) {
		if (fromGridId.equals(toGridId)) {
			return List.of(fromGridId);
		}

		Set<Long> visited = new HashSet<>();
		visited.add(fromGridId);
		Queue<Long> queue = new ArrayDeque<>();
		queue.add(fromGridId);
		Map<Long, Long> cameFrom = new HashMap<>();
		cameFrom.put(fromGridId, null);

		while (!queue.isEmpty()) {
			Long currentGridId = queue.poll();
			if (currentGridId.equals(toGridId)) {
				return reconstructGridIdPath(cameFrom, toGridId);
			}

			for (Long neighborGridId : neighborGridIds(currentGridId, storeGridMap)) {
				if (!storeGridMap.isWalkable(neighborGridId) || !visited.add(neighborGridId)) {
					continue;
				}

				cameFrom.put(neighborGridId, currentGridId);
				queue.add(neighborGridId);
			}
		}

		return List.of();
	}

	private static List<Long> neighborGridIds(Long gridId, StoreGridMap storeGridMap) {
		GridEntity grid = storeGridMap.getGrid(gridId);
		if (grid == null) {
			return List.of();
		}

		List<Long> neighbors = new ArrayList<>(4);
		for (int[] delta : NEIGHBORS) {
			int nextX = grid.getGridX() + delta[0];
			int nextY = grid.getGridY() + delta[1];
			if (nextX < 0 || nextY < 0 || nextX >= storeGridMap.cols() || nextY >= storeGridMap.rows()) {
				continue;
			}

			Long neighborGridId = storeGridMap.gridIdAt(nextX, nextY);
			if (neighborGridId != null) {
				neighbors.add(neighborGridId);
			}
		}
		return neighbors;
	}

	private static List<Long> reconstructGridIdPath(Map<Long, Long> cameFrom, Long goalGridId) {
		List<Long> reversed = new ArrayList<>();
		Long current = goalGridId;
		while (current != null) {
			reversed.add(current);
			current = cameFrom.get(current);
		}

		List<Long> path = new ArrayList<>(reversed.size());
		for (int i = reversed.size() - 1; i >= 0; i--) {
			path.add(reversed.get(i));
		}
		return path;
	}
}
