package com.princesses7.findy.recommendation.recommendation.log.dto.request;

import java.math.BigDecimal;

import com.princesses7.findy.recommendation.recommendation.type.RecommendationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record RecommendationImpressionLogRequest(

	@NotNull(message = "사용자 ID는 필수입니다.")
	Long userId,

	@NotNull(message = "상품 ID는 필수입니다.")
	Long productId,

	Long sourceProductId,

	Long storeId,

	@NotNull(message = "추천 유형은 필수입니다.")
	RecommendationType recommendationType,

	@NotBlank(message = "노출 위치는 필수입니다.")
	String displayLocation,

	@Positive(message = "추천 순위는 1 이상이어야 합니다.")
	Integer recommendationRank,

	@PositiveOrZero(message = "추천 점수는 0 이상이어야 합니다.")
	BigDecimal score,

	String reason
) {
}