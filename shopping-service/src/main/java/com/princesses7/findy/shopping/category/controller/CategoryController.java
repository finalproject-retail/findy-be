package com.princesses7.findy.shopping.category.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.princesses7.findy.shopping.category.dto.response.CategoryNodeResponse;
import com.princesses7.findy.shopping.category.dto.response.CategoryTreeResponse;
import com.princesses7.findy.shopping.category.service.CategoryService;
import com.princesses7.findy.shopping.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@GetMapping
	public ApiResponse<CategoryTreeResponse> getCategories() {
		CategoryTreeResponse response = categoryService.getCategoryTree();
		return ApiResponse.ok("카테고리 목록 조회에 성공했습니다.", response);
	}

	@GetMapping("/{categoryId}")
	public ApiResponse<CategoryNodeResponse> getCategory(@PathVariable Long categoryId) {
		CategoryNodeResponse response = categoryService.getCategory(categoryId);
		return ApiResponse.ok("카테고리 조회에 성공했습니다.", response);
	}
}
