package com.princesses7.findy.user.reward.type;

import com.princesses7.findy.user.reward.entity.RewardHistory;

public enum RewardHistoryFilter {
	ALL,
	EARNED,
	USED_EXPIRED;

	public static RewardHistoryFilter from(String value) {
		if (value == null || value.isBlank()) {
			return ALL;
		}

		return switch (value.trim().toLowerCase()) {
			case "earned" -> EARNED;
			case "used_expired" -> USED_EXPIRED;
			default -> ALL;
		};
	}

	public boolean matches(RewardHistory history) {
		return switch (this) {
			case ALL -> true;
			case EARNED -> history.getRewardType() == RewardType.PURCHASE && history.getRewardAmount() > 0;
			case USED_EXPIRED -> history.getRewardType() == RewardType.USE;
		};
	}
}
