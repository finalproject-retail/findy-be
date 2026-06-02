package com.princesses7.findy.shopping.analytics.event;

import java.util.Locale;

/**
 * 상품 상세 조회 유입 경로.
 * 지도 행사 핀 탭 시 {@link #MAP_PROMOTION} 을 사용한다.
 */
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
