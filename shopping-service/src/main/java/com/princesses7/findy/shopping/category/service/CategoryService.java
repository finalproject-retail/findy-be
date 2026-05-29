package com.princesses7.findy.shopping.category.service;

import static com.princesses7.findy.shopping.global.exception.ErrorCode.CATEGORY_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.shopping.category.dto.response.CategoryBreadcrumbResponse;
import com.princesses7.findy.shopping.category.dto.response.CategoryNodeResponse;
import com.princesses7.findy.shopping.category.dto.response.CategoryTreeResponse;
import com.princesses7.findy.shopping.category.entity.Category;
import com.princesses7.findy.shopping.category.repository.CategoryRepository;
import com.princesses7.findy.shopping.global.exception.BaseException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryTreeResponse getCategoryTree() {
		List<Category> categories = categoryRepository.findByActiveTrueOrderByCategoryIdAsc();
		Map<Long, Category> categoryById = toCategoryById(categories);
		return new CategoryTreeResponse(buildTree(categories, categoryById));
	}

	public CategoryNodeResponse getCategory(Long categoryId) {
		List<Category> categories = categoryRepository.findByActiveTrueOrderByCategoryIdAsc();
		Map<Long, Category> categoryById = toCategoryById(categories);

		Category category = categoryById.values().stream()
			.filter(item -> item.getCategoryId().equals(categoryId))
			.findFirst()
			.orElseThrow(() -> new BaseException(CATEGORY_NOT_FOUND));

		List<CategoryNodeResponse> children = buildSubtree(categories, categoryId, categoryById);

		return CategoryNodeResponse.from(
			category,
			buildBreadcrumb(category, categoryById),
			children
		);
	}

	private List<CategoryNodeResponse> buildTree(
		List<Category> categories,
		Map<Long, Category> categoryById
	) {
		Map<Long, List<Category>> childrenByParentId = groupChildrenByParentId(categories);
		List<Category> rootCategories = categories.stream()
			.filter(category -> category.getParentCategoryId() == null)
			.sorted(Comparator.comparing(Category::getCategoryId))
			.toList();

		return rootCategories.stream()
			.map(category -> toNode(category, childrenByParentId, categoryById))
			.toList();
	}

	private List<CategoryNodeResponse> buildSubtree(
		List<Category> categories,
		Long rootCategoryId,
		Map<Long, Category> categoryById
	) {
		Map<Long, List<Category>> childrenByParentId = groupChildrenByParentId(categories);
		List<Category> childCategories = childrenByParentId.getOrDefault(rootCategoryId, List.of());

		return childCategories.stream()
			.sorted(Comparator.comparing(Category::getCategoryId))
			.map(category -> toNode(category, childrenByParentId, categoryById))
			.toList();
	}

	private CategoryNodeResponse toNode(
		Category category,
		Map<Long, List<Category>> childrenByParentId,
		Map<Long, Category> categoryById
	) {
		List<Category> childCategories = childrenByParentId.getOrDefault(category.getCategoryId(), List.of());
		List<CategoryNodeResponse> children = childCategories.stream()
			.sorted(Comparator.comparing(Category::getCategoryId))
			.map(child -> toNode(child, childrenByParentId, categoryById))
			.toList();

		return CategoryNodeResponse.from(
			category,
			buildBreadcrumb(category, categoryById),
			children
		);
	}

	private List<CategoryBreadcrumbResponse> buildBreadcrumb(
		Category category,
		Map<Long, Category> categoryById
	) {
		List<CategoryBreadcrumbResponse> path = new ArrayList<>();
		Category current = category;

		while (current != null) {
			path.add(new CategoryBreadcrumbResponse(
				current.getCategoryId(),
				current.getCategoryName()
			));

			Long parentCategoryId = current.getParentCategoryId();
			if (parentCategoryId == null) {
				break;
			}

			current = categoryById.get(parentCategoryId);
		}

		Collections.reverse(path);
		return path;
	}

	private Map<Long, Category> toCategoryById(List<Category> categories) {
		return categories.stream()
			.collect(Collectors.toMap(Category::getCategoryId, Function.identity()));
	}

	private Map<Long, List<Category>> groupChildrenByParentId(List<Category> categories) {
		Map<Long, List<Category>> childrenByParentId = new LinkedHashMap<>();

		for (Category category : categories) {
			Long parentCategoryId = category.getParentCategoryId();
			if (parentCategoryId == null) {
				continue;
			}

			childrenByParentId
				.computeIfAbsent(parentCategoryId, ignored -> new ArrayList<>())
				.add(category);
		}

		return childrenByParentId;
	}
}
