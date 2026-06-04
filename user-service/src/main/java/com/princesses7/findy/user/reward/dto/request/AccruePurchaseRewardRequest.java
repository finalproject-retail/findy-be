package com.princesses7.findy.user.reward.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AccruePurchaseRewardRequest(
	@NotNull(message = "주문 ID는 필수입니다.")
	Long orderId,

	@NotNull(message = "최종 결제 금액은 필수입니다.")
	@PositiveOrZero(message = "최종 결제 금액은 0원 이상이어야 합니다.")
	Long finalAmount,

	@NotNull(message = "등급 산정 금액은 필수입니다.")
	@PositiveOrZero(message = "등급 산정 금액은 0원 이상이어야 합니다.")
	Long gradeBaseAmount
) {
}