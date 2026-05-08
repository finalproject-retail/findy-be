package com.princesses7.findy.product.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "상품을 찾을 수 없습니다."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PR002", "상품 상태가 올바르지 않습니다."),
    PRODUCT_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PR003", "상품 위치 정보가 없습니다."),
    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "PR004", "상품 재고 정보를 찾을 수 없습니다."),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "PR005", "검색어가 올바르지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PR006", "카테고리를 찾을 수 없습니다."),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "PR007", "지원하지 않는 정렬 기준입니다."),
    DUPLICATE_BARCODE(HttpStatus.CONFLICT, "PR008", "이미 등록된 바코드입니다."),
    BARCODE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR009", "등록되지 않은 바코드입니다."),
    SEARCH_LOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR010", "검색 로그 저장에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}