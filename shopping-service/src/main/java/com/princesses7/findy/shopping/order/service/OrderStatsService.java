package com.princesses7.findy.shopping.order.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.order.dto.response.FrequentPurchaseProductListResponse;
import com.princesses7.findy.shopping.order.dto.response.FrequentPurchaseProductResponse;
import com.princesses7.findy.shopping.order.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatsService {

	private static final int DEFAULT_DAYS = 90;
	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 20;

	private final OrderItemRepository orderItemRepository;

	@Transactional(readOnly = true)
	public FrequentPurchaseProductListResponse getFrequentPurchaseProducts(
		Long userId,
		LocalDate fromDate,
		LocalDate toDate,
		Integer limit
	) {
		validateUserId(userId);

		LocalDate resolvedToDate = resolveToDate(toDate);
		LocalDate resolvedFromDate = resolveFromDate(fromDate, resolvedToDate);
		validatePeriod(resolvedFromDate, resolvedToDate);

		int resolvedLimit = resolveLimit(limit);

		LocalDateTime fromDateTime = resolvedFromDate.atStartOfDay();
		LocalDateTime toDateTime = resolvedToDate.plusDays(1).atStartOfDay();

		List<FrequentPurchaseProductResponse> products = orderItemRepository.findFrequentPurchaseProducts(
				userId,
				fromDateTime,
				toDateTime,
				PageRequest.of(0, resolvedLimit)
			)
			.stream()
			.map(FrequentPurchaseProductResponse::from)
			.toList();

		return new FrequentPurchaseProductListResponse(
			userId,
			resolvedFromDate,
			resolvedToDate,
			resolvedLimit,
			products
		);
	}

	private void validateUserId(Long userId) {
		if (userId == null) {
			throw new BaseException(UNAUTHORIZED);
		}
	}

	private LocalDate resolveToDate(LocalDate toDate) {
		if (toDate == null) {
			return LocalDate.now();
		}

		return toDate;
	}

	private LocalDate resolveFromDate(LocalDate fromDate, LocalDate toDate) {
		if (fromDate == null) {
			return toDate.minusDays(DEFAULT_DAYS - 1L);
		}

		return fromDate;
	}

	private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
		if (fromDate.isAfter(toDate)) {
			throw new BaseException(INVALID_ANALYTICS_PERIOD);
		}
	}

	private int resolveLimit(Integer limit) {
		if (limit == null || limit < 1) {
			return DEFAULT_LIMIT;
		}

		return Math.min(limit, MAX_LIMIT);
	}
}