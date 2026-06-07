package com.princesses7.findy.user.reward.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.reward.dto.response.RewardHistoryListResponse;
import com.princesses7.findy.user.reward.service.RewardHistoryService;
import com.princesses7.findy.user.reward.type.RewardHistoryFilter;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/rewards")
public class RewardHistoryController {

	private final RewardHistoryService rewardHistoryService;

	@GetMapping
	public ApiResponse<RewardHistoryListResponse> getRewardHistories(
		@RequestHeader("X-USER-ID") Long userId,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
		@RequestParam(defaultValue = "all") String filter,
		@RequestParam(defaultValue = "50") int limit
	) {
		RewardHistoryListResponse response = rewardHistoryService.getRewardHistories(
			userId,
			fromDate,
			toDate,
			RewardHistoryFilter.from(filter),
			limit
		);

		return ApiResponse.ok("포인트 내역 조회에 성공했습니다.", response);
	}
}
