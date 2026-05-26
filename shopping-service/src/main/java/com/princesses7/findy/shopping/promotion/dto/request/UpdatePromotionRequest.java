package com.princesses7.findy.shopping.promotion.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.princesses7.findy.shopping.promotion.entity.PromotionType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePromotionRequest(

	@NotBlank(message = "행사명은 필수입니다.")
	String promotionName,

	@NotNull(message = "행사 유형은 필수입니다.")
	PromotionType promotionType,

	@Min(value = 0, message = "최소 구매 금액은 0원 이상이어야 합니다.")
	Integer minPurchaseAmount,

	@Min(value = 1, message = "구매 조건 수량은 1 이상이어야 합니다.")
	Integer buyQuantity,

	@Min(value = 1, message = "증정 수량은 1 이상이어야 합니다.")
	Integer getQuantity,

	String giftItem,

	@DecimalMin(value = "0.0", message = "할인율은 0 이상이어야 합니다.")
	@DecimalMax(value = "100.0", message = "할인율은 100 이하여야 합니다.")
	BigDecimal discountRate,

	@NotNull(message = "시작일시는 필수입니다.")
	LocalDateTime startAt,

	@NotNull(message = "종료일시는 필수입니다.")
	LocalDateTime endAt
) {
}