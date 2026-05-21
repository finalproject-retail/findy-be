package com.princesses7.findy.recommendation.preference.dto.response;

import java.util.List;

public record UserPreferenceResponse(
	Long userId,
	List<String> preferredCategories,
	List<String> shoppingStyles,
	String preferenceText
) {
}