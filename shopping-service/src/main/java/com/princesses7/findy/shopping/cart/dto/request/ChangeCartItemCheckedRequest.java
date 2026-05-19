package com.princesses7.findy.shopping.cart.dto.request;

import jakarta.validation.constraints.NotNull;

public record ChangeCartItemCheckedRequest(

	@NotNull(message = "선택 여부는 필수입니다.")
	Boolean checked
) {
}