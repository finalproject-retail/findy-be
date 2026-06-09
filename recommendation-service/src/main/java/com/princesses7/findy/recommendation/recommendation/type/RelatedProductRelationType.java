package com.princesses7.findy.recommendation.recommendation.type;

public enum RelatedProductRelationType {

	COMPLEMENTARY,
	SUBSTITUTE,
	RECIPE_INGREDIENT,
	UNRELATED;

	public static RelatedProductRelationType from(String value) {
		if (value == null || value.isBlank()) {
			return UNRELATED;
		}

		for (RelatedProductRelationType relationType : values()) {
			if (relationType.name().equalsIgnoreCase(value.trim())) {
				return relationType;
			}
		}

		return UNRELATED;
	}
}