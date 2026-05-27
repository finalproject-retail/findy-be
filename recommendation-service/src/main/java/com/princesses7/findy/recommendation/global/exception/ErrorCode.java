package com.princesses7.findy.recommendation.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 요청입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "접근 권한이 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),

	// Recommendation
	RECOMMENDATION_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_001", "추천 대상 상품을 찾을 수 없습니다."),
	RECOMMENDATION_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "RECOMMENDATION_002", "상품 카테고리를 찾을 수 없습니다."),
	RECOMMENDATION_EMBEDDING_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "RECOMMENDATION_003", "임베딩 생성에 실패했습니다."),
	RECOMMENDATION_EMPTY_EMBEDDING(HttpStatus.SERVICE_UNAVAILABLE, "RECOMMENDATION_004", "생성된 임베딩 값이 비어 있습니다."),
	RECOMMENDATION_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "RECOMMENDATION_005", "추천 요청 값이 올바르지 않습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}