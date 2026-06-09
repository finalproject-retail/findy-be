package com.princesses7.findy.analytics.analytics.support;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.princesses7.findy.analytics.global.exception.BaseException;
import com.princesses7.findy.analytics.global.exception.ErrorCode;

class AnalyticsPeriodResolverTest {

	private final AnalyticsPeriodResolver analyticsPeriodResolver = new AnalyticsPeriodResolver();

	@Test
	@DisplayName("시작일과 종료일이 모두 있으면 해당 기간으로 조회 범위를 생성한다")
	void resolvePeriodWithFromDateAndToDate() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 7);

		PeriodRange periodRange = analyticsPeriodResolver.resolve(fromDate, toDate);

		assertThat(periodRange.startDate()).isEqualTo(fromDate);
		assertThat(periodRange.endDate()).isEqualTo(toDate);
		assertThat(periodRange.fromAt()).isEqualTo(fromDate.atStartOfDay());
		assertThat(periodRange.toExclusiveAt()).isEqualTo(toDate.plusDays(1).atStartOfDay());
	}

	@Test
	@DisplayName("시작일이 종료일보다 늦으면 예외가 발생한다")
	void throwExceptionWhenFromDateIsAfterToDate() {
		LocalDate fromDate = LocalDate.of(2026, 5, 10);
		LocalDate toDate = LocalDate.of(2026, 5, 1);

		assertThatThrownBy(() -> analyticsPeriodResolver.resolve(fromDate, toDate))
			.isInstanceOf(BaseException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ANALYTICS_INVALID_PERIOD);
	}

	@Test
	@DisplayName("limit이 null이면 기본 최대 조회 개수를 반환한다")
	void resolveDefaultLimit() {
		int limit = analyticsPeriodResolver.resolveLimit(null);

		assertThat(limit).isEqualTo(100);
	}

	@Test
	@DisplayName("limit이 1 이상 100 이하이면 그대로 반환한다")
	void resolveValidLimit() {
		int limit = analyticsPeriodResolver.resolveLimit(20);

		assertThat(limit).isEqualTo(20);
	}

	@Test
	@DisplayName("limit이 0 이하이면 예외가 발생한다")
	void throwExceptionWhenLimitIsZeroOrNegative() {
		assertThatThrownBy(() -> analyticsPeriodResolver.resolveLimit(0))
			.isInstanceOf(BaseException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ANALYTICS_INVALID_LIMIT);
	}

	@Test
	@DisplayName("limit이 100을 초과하면 예외가 발생한다")
	void throwExceptionWhenLimitIsGreaterThanMax() {
		assertThatThrownBy(() -> analyticsPeriodResolver.resolveLimit(101))
			.isInstanceOf(BaseException.class)
			.extracting("errorCode")
			.isEqualTo(ErrorCode.ANALYTICS_INVALID_LIMIT);
	}
}