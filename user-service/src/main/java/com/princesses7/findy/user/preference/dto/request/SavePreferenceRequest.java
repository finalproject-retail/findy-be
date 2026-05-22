package com.princesses7.findy.user.preference.dto.request;

import java.util.List;

public record SavePreferenceRequest(
	List<Long> categoryIds,
	List<Long> shoppingStyleIds
) {
}