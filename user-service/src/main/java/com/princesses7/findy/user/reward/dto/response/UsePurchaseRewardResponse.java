package com.princesses7.findy.user.reward.dto.response;

public record UsePurchaseRewardResponse(
	Long userId,
	Long orderId,
	long usedReward,
	long rewardBalance
) {
}
