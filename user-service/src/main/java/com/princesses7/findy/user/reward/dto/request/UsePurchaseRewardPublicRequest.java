package com.princesses7.findy.user.reward.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UsePurchaseRewardPublicRequest(
	@NotNull(message = "주문 ID는 필수입니다.")
	Long orderId,

	@NotNull(message = "사용 포인트는 필수입니다.")
	@Positive(message = "사용 포인트는 1 이상이어야 합니다.")
	Long usedAmount
) {
}
