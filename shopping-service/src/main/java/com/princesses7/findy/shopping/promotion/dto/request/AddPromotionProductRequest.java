package com.princesses7.findy.shopping.promotion.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddPromotionProductRequest(

	@NotNull(message = "상품 ID는 필수입니다.")
	Long productId,

	@Min(value = 0, message = "행사 가격은 0원 이상이어야 합니다.")
	Integer promotionPrice,

	@NotNull(message = "그리드 ID는 필수입니다.")
	@Min(value = 1, message = "그리드 ID는 1 이상이어야 합니다.")
	Long gridId
) {
}