package com.princesses7.findy.analytics.event.dto;

import java.util.Locale;

public enum ProductViewSource {

	DIRECT,
	MAP_PROMOTION;

	public static ProductViewSource fromNullable(String value) {
		if (value == null || value.isBlank()) {
			return DIRECT;
		}

		try {
			return ProductViewSource.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			return DIRECT;
		}
	}
}
