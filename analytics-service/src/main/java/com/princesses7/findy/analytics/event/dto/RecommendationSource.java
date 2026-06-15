package com.princesses7.findy.analytics.event.dto;

import java.util.Locale;

public enum RecommendationSource {

	DIRECT,
	SEARCH,
	RECOMMENDATION,
	POPULAR,
	PROMOTION,
	SUBSTITUTE;

	public static RecommendationSource fromNullable(String value) {
		if (value == null || value.isBlank()) {
			return DIRECT;
		}

		try {
			return RecommendationSource.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return DIRECT;
		}
	}
}
