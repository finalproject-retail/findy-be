package com.princesses7.findy.analytics.analytics.support;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PeriodRange(
	LocalDate fromDate,
	LocalDate toDate,
	LocalDateTime fromAt,
	LocalDateTime toExclusiveAt
) {

	public static PeriodRange of(LocalDate fromDate, LocalDate toDate) {
		return new PeriodRange(
			fromDate,
			toDate,
			fromDate.atStartOfDay(),
			toDate.plusDays(1).atStartOfDay()
		);
	}
}