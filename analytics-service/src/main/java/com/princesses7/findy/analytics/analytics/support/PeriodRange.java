package com.princesses7.findy.analytics.analytics.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodRange(
	LocalDate startDate,
	LocalDate endDate,
	LocalDateTime fromAt,
	LocalDateTime toExclusiveAt
) {

	public static PeriodRange of(LocalDate startDate, LocalDate endDate) {
		return new PeriodRange(
			startDate,
			endDate,
			startDate.atStartOfDay(),
			endDate.plusDays(1).atStartOfDay()
		);
	}
}