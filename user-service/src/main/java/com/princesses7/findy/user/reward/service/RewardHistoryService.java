package com.princesses7.findy.user.reward.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.reward.dto.response.RewardHistoryItemResponse;
import com.princesses7.findy.user.reward.dto.response.RewardHistoryListResponse;
import com.princesses7.findy.user.reward.entity.RewardHistory;
import com.princesses7.findy.user.reward.repository.RewardHistoryRepository;
import com.princesses7.findy.user.reward.type.RewardHistoryFilter;
import com.princesses7.findy.user.reward.type.RewardType;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardHistoryService {

	private static final int DEFAULT_LIMIT = 50;
	private static final int MAX_LIMIT = 100;
	private static final LocalDateTime MIN_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);
	private static final LocalDateTime MAX_DATE_TIME = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

	private final UserRepository userRepository;
	private final RewardHistoryRepository rewardHistoryRepository;

	public RewardHistoryListResponse getRewardHistories(
		Long userId,
		LocalDate fromDate,
		LocalDate toDate,
		RewardHistoryFilter filter,
		int limit
	) {
		validateUser(userId);

		int resolvedLimit = resolveLimit(limit);
		LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : MIN_DATE_TIME;
		LocalDateTime toDateTime = toDate != null ? toDate.plusDays(1).atStartOfDay() : MAX_DATE_TIME;

		List<RewardHistoryItemResponse> histories = rewardHistoryRepository
			.findHistories(userId, fromDateTime, toDateTime, PageRequest.of(0, resolvedLimit))
			.stream()
			.filter(filter::matches)
			.map(this::toItemResponse)
			.toList();

		return new RewardHistoryListResponse(histories, histories.size());
	}

	private void validateUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new BaseException(ErrorCode.USER_NOT_FOUND);
		}
	}

	private int resolveLimit(int limit) {
		if (limit <= 0) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	private RewardHistoryItemResponse toItemResponse(RewardHistory history) {
		return new RewardHistoryItemResponse(
			history.getRewardHistoryId(),
			resolveHistoryType(history),
			history.getCreatedAt().toLocalDate(),
			resolveTitle(history),
			resolveSubtitle(history),
			resolveAmount(history)
		);
	}

	private String resolveHistoryType(RewardHistory history) {
		if (history.getRewardType() == RewardType.PURCHASE) {
			return "earned";
		}
		return "used";
	}

	private String resolveTitle(RewardHistory history) {
		if (history.getRewardType() == RewardType.PURCHASE) {
			return "구매 포인트 지급";
		}
		return "포인트 사용";
	}

	private String resolveSubtitle(RewardHistory history) {
		if (history.getOrderId() == null) {
			return "";
		}
		return "주문번호: " + history.getOrderId();
	}

	private long resolveAmount(RewardHistory history) {
		if (history.getRewardType() == RewardType.PURCHASE) {
			return history.getRewardAmount();
		}
		return -Math.abs(history.getRewardAmount());
	}
}
