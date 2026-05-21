package com.princesses7.findy.recommendation.preference.dto.response;

import java.util.List;

public record UserPreferenceResponse(
	Long userId,
	List<Long> preferredCategoryIds,
	List<String> preferredCategories,
	List<Long> shoppingStyleIds,
	List<String> shoppingStyles,
	String preferenceText
) {

	public boolean hasPreference() {
		return !preferredCategoryIds.isEmpty()
			|| !shoppingStyleIds.isEmpty()
			|| !preferredCategories.isEmpty()
			|| !shoppingStyles.isEmpty();
	}

	public boolean hasPreferredCategory(Long categoryId) {
		return categoryId != null && preferredCategoryIds.contains(categoryId);
	}

	public boolean hasShoppingStyle(String styleName) {
		return styleName != null && shoppingStyles.contains(styleName);
	}
}