package com.princesses7.findy.recommendation.recommendation.log.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RecommendationPurchaseConversionRequest(

	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,

	@NotNull(message = "주문 ID는 필수입니다.")
	Long orderId,

	@NotEmpty(message = "추천 로그 ID 목록은 필수입니다.")
	List<Long> recommendationLogIds,

	@NotEmpty(message = "구매 상품 ID 목록은 필수입니다.")
	List<Long> purchasedProductIds
) {
}