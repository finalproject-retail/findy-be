package com.princesses7.findy.analytics.zone.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.princesses7.findy.analytics.analytics.support.AnalyticsPeriodResolver;
import com.princesses7.findy.analytics.analytics.support.PeriodRange;
import com.princesses7.findy.analytics.zone.dto.query.ZoneMovementQueryResult;
import com.princesses7.findy.analytics.zone.dto.query.ZoneVisitRateQueryResult;
import com.princesses7.findy.analytics.zone.dto.response.ZoneVisitRateAnalyticsResponse;
import com.princesses7.findy.analytics.zone.repository.ZoneVisitRateAnalyticsRepository;

@ExtendWith(MockitoExtension.class)
class ZoneVisitRateAnalyticsServiceTest {

	@Mock
	private ZoneVisitRateAnalyticsRepository zoneVisitRateAnalyticsRepository;

	@Mock
	private AnalyticsPeriodResolver analyticsPeriodResolver;

	@InjectMocks
	private ZoneVisitRateAnalyticsService zoneVisitRateAnalyticsService;

	@Test
	@DisplayName("구역별 방문율과 이동 요약을 함께 조회한다")
	void getZoneVisitRatesWithMovements() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Long storeId = 1L;
		Integer minStaySeconds = 5;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(zoneVisitRateAnalyticsRepository.findZoneVisitRates(periodRange, storeId, null, minStaySeconds))
			.willReturn(List.of(
				new ZoneVisitRateQueryResult(
					1L,
					"과일",
					10L,
					7L,
					new BigDecimal("62.50"),
					45L,
					450L,
					1
				)
			));
		given(zoneVisitRateAnalyticsRepository.findZoneMovements(periodRange, storeId, null, minStaySeconds))
			.willReturn(List.of(
				new ZoneMovementQueryResult(
					1L,
					"과일",
					2L,
					"채소/샐러드",
					4L,
					new BigDecimal("40.00"),
					12L
				)
			));

		ZoneVisitRateAnalyticsResponse response = zoneVisitRateAnalyticsService.getZoneVisitRates(
			fromDate,
			toDate,
			storeId,
			null,
			minStaySeconds,
			true
		);

		assertThat(response.period().fromDate()).isEqualTo(fromDate);
		assertThat(response.period().toDate()).isEqualTo(toDate);
		assertThat(response.storeId()).isEqualTo(storeId);
		assertThat(response.minStaySeconds()).isEqualTo(minStaySeconds);
		assertThat(response.includeMovement()).isTrue();
		assertThat(response.zoneVisitRates()).hasSize(1);
		assertThat(response.zoneVisitRates().get(0).zoneName()).isEqualTo("과일");
		assertThat(response.zoneVisitRates().get(0).visitRate()).isEqualByComparingTo("62.50");
		assertThat(response.zoneMovements()).hasSize(1);
		assertThat(response.zoneMovements().get(0).fromZoneName()).isEqualTo("과일");
		assertThat(response.zoneMovements().get(0).toZoneName()).isEqualTo("채소/샐러드");
	}

	@Test
	@DisplayName("이동 요약 포함 옵션이 false이면 구역별 방문율만 조회한다")
	void getZoneVisitRatesWithoutMovements() {
		LocalDate fromDate = LocalDate.of(2026, 5, 1);
		LocalDate toDate = LocalDate.of(2026, 5, 31);
		Long storeId = 1L;
		PeriodRange periodRange = PeriodRange.of(fromDate, toDate);

		given(analyticsPeriodResolver.resolve(fromDate, toDate))
			.willReturn(periodRange);
		given(zoneVisitRateAnalyticsRepository.findZoneVisitRates(periodRange, storeId, null, 5))
			.willReturn(List.of());

		ZoneVisitRateAnalyticsResponse response = zoneVisitRateAnalyticsService.getZoneVisitRates(
			fromDate,
			toDate,
			storeId,
			null,
			null,
			false
		);

		assertThat(response.minStaySeconds()).isEqualTo(5);
		assertThat(response.includeMovement()).isFalse();
		assertThat(response.zoneVisitRates()).isEmpty();
		assertThat(response.zoneMovements()).isEmpty();
		then(zoneVisitRateAnalyticsRepository).should(never())
			.findZoneMovements(any(), any(), any(), anyInt());
	}
}