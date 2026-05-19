package com.princesses7.findy.shopping.global.exception;

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

	// Cart
	CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "장바구니를 찾을 수 없습니다."),
	CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_002", "장바구니 상품을 찾을 수 없습니다."),
	INVALID_CART_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "장바구니 상품 수량이 올바르지 않습니다."),
	CART_ITEM_NOT_SELECTED(HttpStatus.BAD_REQUEST, "CART_004", "선택된 장바구니 상품이 없습니다."),
	CART_ITEM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CART_005", "이미 장바구니에 담긴 상품입니다."),
	EMPTY_CART(HttpStatus.BAD_REQUEST, "CART_006", "장바구니가 비어 있습니다."),
	EXCEED_CART_ITEM_STOCK(HttpStatus.BAD_REQUEST, "CART_007", "장바구니 수량이 재고 수량을 초과할 수 없습니다."),

	// Shopping List
	SHOPPING_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOPPING_LIST_001", "쇼핑리스트를 찾을 수 없습니다."),
	SHOPPING_LIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "SHOPPING_LIST_002", "쇼핑리스트 상품을 찾을 수 없습니다."),
	EMPTY_SHOPPING_LIST(HttpStatus.BAD_REQUEST, "SHOPPING_LIST_003", "쇼핑리스트에 담을 상품이 없습니다."),
	INVALID_SHOPPING_LIST_QUANTITY(HttpStatus.BAD_REQUEST, "SHOPPING_LIST_004", "쇼핑리스트 상품 수량이 올바르지 않습니다."),
	SHOPPING_LIST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "SHOPPING_LIST_005", "본인의 쇼핑리스트만 접근할 수 있습니다."),
	SHOPPING_LIST_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "SHOPPING_LIST_006", "이미 완료된 쇼핑리스트입니다."),
	SHOPPING_LIST_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "SHOPPING_LIST_007", "이미 취소된 쇼핑리스트입니다."),
	INVALID_SHOPPING_LIST_STATUS(HttpStatus.BAD_REQUEST, "SHOPPING_LIST_008", "쇼핑리스트 상태가 올바르지 않습니다."),
	DUPLICATE_SHOPPING_LIST_ITEM(HttpStatus.CONFLICT, "SHOPPING_LIST_009", "이미 쇼핑리스트에 담긴 상품입니다."),
	SHOPPING_LIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "SHOPPING_LIST_010", "이미 생성된 쇼핑리스트가 있습니다."),

	// Scan
	INVALID_BARCODE(HttpStatus.BAD_REQUEST, "SCAN_001", "등록되지 않은 바코드입니다."),
	BARCODE_SCAN_FAILED(HttpStatus.BAD_REQUEST, "SCAN_002", "바코드 인식에 실패했습니다."),
	ALREADY_SCANNED_ITEM(HttpStatus.BAD_REQUEST, "SCAN_003", "이미 스캔 완료된 상품입니다."),
	NOT_SCANNED_ITEM(HttpStatus.BAD_REQUEST, "SCAN_004", "스캔되지 않은 상품입니다."),
	INVALID_SCAN_STATUS(HttpStatus.BAD_REQUEST, "SCAN_005", "스캔 상태가 올바르지 않습니다."),
	INVALID_ENTRY_TYPE(HttpStatus.BAD_REQUEST, "SCAN_006", "쇼핑리스트 상품 진입 유형이 올바르지 않습니다."),
	SCAN_QUANTITY_EXCEEDED(HttpStatus.BAD_REQUEST, "SCAN_007", "스캔 수량이 상품 수량을 초과할 수 없습니다."),
	UNLISTED_ITEM_SCAN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SCAN_008", "쇼핑리스트에 없는 상품은 스캔 처리할 수 없습니다."),

	// Purchase Target
	PURCHASE_TARGET_NOT_FOUND(HttpStatus.BAD_REQUEST, "PURCHASE_001", "구매 대상 상품이 없습니다."),
	NO_SCANNED_ITEM(HttpStatus.BAD_REQUEST, "PURCHASE_002", "스캔 완료된 상품이 없습니다."),
	UNSCANNED_ITEM_EXISTS(HttpStatus.BAD_REQUEST, "PURCHASE_003", "스캔되지 않은 상품이 남아 있습니다."),
	INVALID_PURCHASE_TARGET(HttpStatus.BAD_REQUEST, "PURCHASE_004", "구매 대상 상품 정보가 올바르지 않습니다."),

	// Product Client
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
	PRODUCT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "PRODUCT_002", "상품 서비스와 통신할 수 없습니다."),
	OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT_003", "품절 상품은 추가할 수 없습니다."),
	EXCEED_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "PRODUCT_004", "재고 수량을 초과할 수 없습니다."),
	PRODUCT_PRICE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PRODUCT_005", "상품 가격 정보를 찾을 수 없습니다."),
	INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PRODUCT_006", "판매 가능한 상품이 아닙니다."),

	// Coupon
	COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_001", "쿠폰을 찾을 수 없습니다."),
	INVALID_COUPON_PERIOD(HttpStatus.BAD_REQUEST, "COUPON_002", "쿠폰 기간 설정이 올바르지 않습니다."),
	INVALID_COUPON_DISCOUNT_VALUE(HttpStatus.BAD_REQUEST, "COUPON_003", "쿠폰 할인 값이 올바르지 않습니다."),
	COUPON_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "COUPON_004", "이미 비활성화된 쿠폰입니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}