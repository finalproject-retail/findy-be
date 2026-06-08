package com.princesses7.findy.user.reward.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.user.global.response.ApiResponse;
import com.princesses7.findy.user.reward.dto.request.AccruePurchaseRewardPublicRequest;
import com.princesses7.findy.user.reward.dto.request.UsePurchaseRewardPublicRequest;
import com.princesses7.findy.user.reward.dto.response.AccruePurchaseRewardResponse;
import com.princesses7.findy.user.reward.dto.response.UsePurchaseRewardResponse;
import com.princesses7.findy.user.reward.service.RewardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me/rewards")
public class PurchaseRewardController {

	private final RewardService rewardService;

	@PostMapping("/orders")
	public ApiResponse<AccruePurchaseRewardResponse> accruePurchaseReward(
		@RequestHeader("X-USER-ID") Long userId,
		@Valid @RequestBody AccruePurchaseRewardPublicRequest request
	) {
		AccruePurchaseRewardResponse response = rewardService.accruePurchaseRewardFromOrder(
			userId,
			request
		);

		return ApiResponse.ok("구매 포인트 적립에 성공했습니다.", response);
	}

	@PostMapping("/use")
	public ApiResponse<UsePurchaseRewardResponse> usePurchaseReward(
		@RequestHeader("X-USER-ID") Long userId,
		@Valid @RequestBody UsePurchaseRewardPublicRequest request
	) {
		UsePurchaseRewardResponse response = rewardService.usePurchaseRewardFromOrder(
			userId,
			request
		);

		return ApiResponse.ok("포인트 사용에 성공했습니다.", response);
	}
}
