package com.princesses7.findy.user.reward.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.reward.dto.request.AccruePurchaseRewardRequest;
import com.princesses7.findy.user.reward.dto.response.AccruePurchaseRewardResponse;
import com.princesses7.findy.user.reward.service.RewardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users/{userId}/rewards")
public class InternalRewardController {

	private final RewardService rewardService;

	@PostMapping("/orders")
	public ApiResponse<AccruePurchaseRewardResponse> accruePurchaseReward(
		@PathVariable Long userId,
		@Valid @RequestBody AccruePurchaseRewardRequest request
	) {
		return ApiResponse.ok(
			"구매 적립금 적립에 성공했습니다.",
			rewardService.accruePurchaseReward(userId, request)
		);
	}
}