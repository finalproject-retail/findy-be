package com.princesses7.findy.user.reward.dto.response;

import com.princesses7.findy.user.user.entity.Grade;

public record AccruePurchaseRewardResponse(
	Long userId,
	Long orderId,
	long earnedReward,
	long rewardBalance,
	long purchaseAmount,
	Grade grade,
	double rewardRate
) {
}