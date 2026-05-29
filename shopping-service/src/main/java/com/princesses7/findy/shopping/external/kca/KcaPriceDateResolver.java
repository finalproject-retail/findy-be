package com.princesses7.findy.shopping.external.kca;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import org.springframework.stereotype.Component;

/*
	조사일 계산 유틸
 */
@Component
public class KcaPriceDateResolver {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	public String resolveLatestFriday() {
		LocalDate today = LocalDate.now();

		if (today.getDayOfWeek() == DayOfWeek.FRIDAY) {
			return today.format(FORMATTER);
		}

		return today.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY))
			.format(FORMATTER);
	}

	public String resolvePreviousFriday() {
		return LocalDate.now()
			.with(TemporalAdjusters.previous(DayOfWeek.FRIDAY))
			.minusWeeks(1)
			.format(FORMATTER);
	}
}