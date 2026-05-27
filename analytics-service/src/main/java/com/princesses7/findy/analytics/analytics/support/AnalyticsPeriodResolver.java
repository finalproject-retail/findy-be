package com.princesses7.findy.analytics.analytics.support;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.princesses7.findy.analytics.global.exception.BaseException;
import com.princesses7.findy.analytics.global.exception.ErrorCode;

@Component
public class AnalyticsPeriodResolver {

	private static final int DEFAULT_PERIOD_DAYS = 7;
	private static final int MAX_LIMIT = 100;

	public PeriodRange resolve(LocalDate fromDate, LocalDate toDate) {
		LocalDate resolvedToDate = toDate == null ? LocalDate.now() : toDate;
		LocalDate resolvedFromDate = fromDate == null
			? resolvedToDate.minusDays(DEFAULT_PERIOD_DAYS - 1L)
			: fromDate;

		if (resolvedFromDate.isAfter(resolvedToDate)) {
			throw new BaseException(ErrorCode.ANALYTICS_INVALID_PERIOD);
		}

		return PeriodRange.of(resolvedFromDate, resolvedToDate);
	}

	public int resolveLimit(Integer limit) {
		if (limit == null) {
			return MAX_LIMIT;
		}

		if (limit <= 0 || limit > MAX_LIMIT) {
			throw new BaseException(ErrorCode.ANALYTICS_INVALID_LIMIT);
		}

		return limit;
	}
}