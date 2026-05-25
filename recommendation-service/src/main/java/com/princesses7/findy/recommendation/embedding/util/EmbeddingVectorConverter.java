package com.princesses7.findy.recommendation.embedding.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class EmbeddingVectorConverter {

	private EmbeddingVectorConverter() {
	}

	public static String toText(List<Double> embedding) {
		if (embedding == null || embedding.isEmpty()) {
			return "";
		}

		return embedding.stream()
			.map(String::valueOf)
			.collect(Collectors.joining(","));
	}

	public static List<Double> toVector(String embeddingText) {
		if (embeddingText == null || embeddingText.isBlank()) {
			return List.of();
		}

		return Arrays.stream(embeddingText.split(","))
			.filter(value -> !value.isBlank())
			.map(Double::parseDouble)
			.toList();
	}
}