package com.princesses7.findy.shopping.category.dto.response;

import java.util.List;

public record CategoryTreeResponse(
	List<CategoryNodeResponse> categories
) {
}
