package com.princesses7.findy.user.reward.dto.response;

import java.time.LocalDate;

public record RewardHistoryItemResponse(
	Long id,
	String type,
	LocalDate date,
	String title,
	String subtitle,
	long amount
) {
}
