package com.princesses7.findy.shopping.category.dto.response;

import java.util.List;

import com.princesses7.findy.shopping.category.entity.Category;

public record CategoryNodeResponse(
	Long categoryId,
	Long parentCategoryId,
	String categoryName,
	Long gridId,
	List<CategoryBreadcrumbResponse> breadcrumb,
	List<CategoryNodeResponse> children
) {

	public static CategoryNodeResponse from(
		Category category,
		List<CategoryBreadcrumbResponse> breadcrumb,
		List<CategoryNodeResponse> children
	) {
		return new CategoryNodeResponse(
			category.getCategoryId(),
			category.getParentCategoryId(),
			category.getCategoryName(),
			category.getGridId(),
			breadcrumb,
			children
		);
	}
}
