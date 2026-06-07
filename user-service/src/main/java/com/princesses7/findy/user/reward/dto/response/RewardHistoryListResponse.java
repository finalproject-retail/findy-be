package com.princesses7.findy.user.reward.dto.response;

import java.util.List;

public record RewardHistoryListResponse(
	List<RewardHistoryItemResponse> histories,
	int count
) {
}
