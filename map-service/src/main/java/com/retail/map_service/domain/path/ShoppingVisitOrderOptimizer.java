package com.retail.map_service.domain.path;

import java.util.ArrayList;
import java.util.List;

/**
 * 출발 통로 격자에서 모든 목적지를 한 번씩 방문할 때 BFS 통로 칸 수 합이 최소가 되도록 방문 순서를 정한다.
 * 프론트 {@code orderShoppingRoute.ts}와 동일: n≤8 전 순열, 그 외 nearest-neighbor + 2-opt.
 */
public final class ShoppingVisitOrderOptimizer {

	private static final int MAX_EXACT_PERMUTE = 8;

	private ShoppingVisitOrderOptimizer() {
	}

	public static List<Long> orderMinimumVisit(
		Long startWalkableGridId,
		List<Long> destinationGridIds,
		StoreGridMap storeGridMap
	) {
		int n = destinationGridIds.size();
		if (n <= 1) {
			return List.copyOf(destinationGridIds);
		}

		Long[] walkableGoals = new Long[n];
		for (int i = 0; i < n; i++) {
			walkableGoals[i] = AislePathfinder.resolveWalkableGridId(destinationGridIds.get(i), storeGridMap);
		}

		int[] startDist = new int[n];
		for (int i = 0; i < n; i++) {
			startDist[i] = walkableGoals[i] == null
				? Integer.MAX_VALUE
				: AislePathfinder.pathStepCount(startWalkableGridId, walkableGoals[i], storeGridMap);
		}

		int[][] itemDist = buildItemDistanceMatrix(walkableGoals, storeGridMap);
		int[] visitIndices = solveMinimumVisitIndices(n, startDist, itemDist);

		List<Long> ordered = new ArrayList<>(n);
		for (int index : visitIndices) {
			ordered.add(destinationGridIds.get(index));
		}
		return ordered;
	}

	private static int[][] buildItemDistanceMatrix(Long[] walkableGoals, StoreGridMap storeGridMap) {
		int n = walkableGoals.length;
		int[][] itemDist = new int[n][n];

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int steps = distanceBetweenGoals(walkableGoals[i], walkableGoals[j], storeGridMap);
				itemDist[i][j] = steps;
				itemDist[j][i] = steps;
			}
		}
		return itemDist;
	}

	private static int distanceBetweenGoals(Long fromGoal, Long toGoal, StoreGridMap storeGridMap) {
		if (fromGoal == null || toGoal == null) {
			return Integer.MAX_VALUE;
		}
		if (fromGoal.equals(toGoal)) {
			return 0;
		}
		return AislePathfinder.pathStepCount(fromGoal, toGoal, storeGridMap);
	}

	private static int[] solveMinimumVisitIndices(int n, int[] startDist, int[][] itemDist) {
		if (n == 1) {
			return new int[] { 0 };
		}

		if (n <= MAX_EXACT_PERMUTE) {
			return solveByPermutation(n, startDist, itemDist);
		}

		return twoOptOpenPath(nearestNeighborOrder(n, startDist, itemDist), startDist, itemDist);
	}

	private static int[] solveByPermutation(int n, int[] startDist, int[][] itemDist) {
		int[] indices = new int[n];
		for (int i = 0; i < n; i++) {
			indices[i] = i;
		}

		int[] bestOrder = indices.clone();
		int bestCost = openPathCost(bestOrder, startDist, itemDist);
		permute(indices, 0, bestOrder, startDist, itemDist, new int[] { bestCost });
		return bestOrder;
	}

	private static void permute(
		int[] indices,
		int start,
		int[] bestOrder,
		int[] startDist,
		int[][] itemDist,
		int[] bestCostHolder
	) {
		if (start == indices.length) {
			int cost = openPathCost(indices, startDist, itemDist);
			if (cost < bestCostHolder[0]) {
				bestCostHolder[0] = cost;
				System.arraycopy(indices, 0, bestOrder, 0, indices.length);
			}
			return;
		}

		for (int i = start; i < indices.length; i++) {
			swap(indices, start, i);
			permute(indices, start + 1, bestOrder, startDist, itemDist, bestCostHolder);
			swap(indices, start, i);
		}
	}

	private static void swap(int[] array, int i, int j) {
		int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	private static int[] nearestNeighborOrder(int n, int[] startDist, int[][] itemDist) {
		boolean[] visited = new boolean[n];
		int[] order = new int[n];
		int current = -1;

		for (int step = 0; step < n; step++) {
			int best = -1;
			int bestDistance = Integer.MAX_VALUE;

			for (int i = 0; i < n; i++) {
				if (visited[i]) {
					continue;
				}
				int distance = current < 0 ? startDist[i] : itemDist[current][i];
				if (distance < bestDistance) {
					bestDistance = distance;
					best = i;
				}
			}

			if (best < 0) {
				break;
			}
			visited[best] = true;
			order[step] = best;
			current = best;
		}

		return order;
	}

	private static int[] twoOptOpenPath(int[] order, int[] startDist, int[][] itemDist) {
		int[] best = order.clone();
		int bestCost = openPathCost(best, startDist, itemDist);
		boolean improved = true;

		while (improved) {
			improved = false;
			for (int i = 0; i < best.length - 1; i++) {
				for (int j = i + 1; j < best.length; j++) {
					int[] candidate = reverseSegment(best, i, j);
					int cost = openPathCost(candidate, startDist, itemDist);
					if (cost < bestCost) {
						best = candidate;
						bestCost = cost;
						improved = true;
					}
				}
			}
		}

		return best;
	}

	private static int[] reverseSegment(int[] order, int fromInclusive, int toInclusive) {
		int[] candidate = order.clone();
		while (fromInclusive < toInclusive) {
			int temp = candidate[fromInclusive];
			candidate[fromInclusive] = candidate[toInclusive];
			candidate[toInclusive] = temp;
			fromInclusive++;
			toInclusive--;
		}
		return candidate;
	}

	private static int openPathCost(int[] order, int[] startDist, int[][] itemDist) {
		if (order.length == 0) {
			return 0;
		}

		long cost = startDist[order[0]];
		if (cost >= Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}

		for (int i = 1; i < order.length; i++) {
			cost += itemDist[order[i - 1]][order[i]];
			if (cost >= Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
		}
		return (int) cost;
	}
}
