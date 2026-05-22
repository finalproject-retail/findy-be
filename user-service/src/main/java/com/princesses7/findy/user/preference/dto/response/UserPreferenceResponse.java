package com.princesses7.findy.user.preference.dto.response;

import java.util.List;

public record UserPreferenceResponse(
	Long userId,
	List<Long> categoryIds,
	List<Long> shoppingStyleIds
) {
}