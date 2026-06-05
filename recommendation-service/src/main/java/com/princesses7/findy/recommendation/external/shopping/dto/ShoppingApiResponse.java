package com.princesses7.findy.recommendation.external.shopping.dto;

public record ShoppingApiResponse<T>(
	boolean success,
	String code,
	String message,
	T data
) {
}
