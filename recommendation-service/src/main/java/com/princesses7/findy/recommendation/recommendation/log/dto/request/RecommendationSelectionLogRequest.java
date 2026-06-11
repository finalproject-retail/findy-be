package com.princesses7.findy.recommendation.recommendation.log.dto.request;

import jakarta.validation.constraints.NotNull;

public record RecommendationSelectionLogRequest(

	@NotNull(message = "추천 로그 ID는 필수입니다.")
	Long recommendationLogId,

	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,

	@NotNull(message = "기준 상품 ID는 필수입니다.")
	Long sourceProductId,

	@NotNull(message = "선택 상품 ID는 필수입니다.")
	Long selectedProductId
) {
}