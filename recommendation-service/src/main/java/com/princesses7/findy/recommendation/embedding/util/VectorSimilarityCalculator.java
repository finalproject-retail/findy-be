package com.princesses7.findy.recommendation.embedding.util;

import java.util.List;

public final class VectorSimilarityCalculator {

	private VectorSimilarityCalculator() {
	}

	public static double cosineSimilarity(
		List<Double> source,
		List<Double> target
	) {
		if (source == null || target == null || source.isEmpty() || target.isEmpty()) {
			return 0.0;
		}

		int size = Math.min(source.size(), target.size());

		double dotProduct = 0.0;
		double sourceNorm = 0.0;
		double targetNorm = 0.0;

		for (int i = 0; i < size; i++) {
			double sourceValue = source.get(i);
			double targetValue = target.get(i);

			dotProduct += sourceValue * targetValue;
			sourceNorm += sourceValue * sourceValue;
			targetNorm += targetValue * targetValue;
		}

		if (sourceNorm == 0.0 || targetNorm == 0.0) {
			return 0.0;
		}

		return dotProduct / (Math.sqrt(sourceNorm) * Math.sqrt(targetNorm));
	}
}