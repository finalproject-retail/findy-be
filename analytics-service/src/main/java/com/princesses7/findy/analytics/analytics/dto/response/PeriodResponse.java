package com.princesses7.findy.analytics.analytics.dto.response;

import java.time.LocalDate;

import com.princesses7.findy.analytics.analytics.support.PeriodRange;

public record PeriodResponse(
	LocalDate fromDate,
	LocalDate toDate
) {

	public static PeriodResponse from(PeriodRange periodRange) {
		return new PeriodResponse(
			periodRange.fromDate(),
			periodRange.toDate()
		);
	}
}