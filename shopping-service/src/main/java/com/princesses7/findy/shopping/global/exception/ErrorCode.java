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

	// Product
	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
	INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PRODUCT_002", "상품 상태가 올바르지 않습니다."),
	PRODUCT_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_003", "상품 위치 정보가 없습니다."),
	PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_004", "상품 재고 정보를 찾을 수 없습니다."),
	INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "PRODUCT_005", "검색어가 올바르지 않습니다."),
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_006", "카테고리를 찾을 수 없습니다."),
	INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "PRODUCT_007", "지원하지 않는 정렬 기준입니다."),
	DUPLICATE_BARCODE(HttpStatus.CONFLICT, "PRODUCT_008", "이미 등록된 바코드입니다."),
	BARCODE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_009", "바코드에 해당하는 상품을 찾을 수 없습니다."),
	SEARCH_LOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_010", "검색 로그 저장에 실패했습니다."),
	PRODUCT_NOT_REGISTERED(HttpStatus.NOT_FOUND, "PRODUCT_011", "매장에 등록되지 않은 상품입니다."),
	CATEGORY_CLASSIFICATION_FAILED(HttpStatus.BAD_REQUEST, "PRODUCT_012", "상품 카테고리 분류에 실패했습니다."),
	HACCP_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_013", "HACCP에서 상품 정보를 찾을 수 없습니다."),

	// Inventory
	INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "INVENTORY_001", "재고 정보를 찾을 수 없습니다."),
	INVENTORY_INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INVENTORY_002", "상품 재고가 부족합니다."),
	INVALID_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "INVENTORY_003", "차감할 재고 수량이 올바르지 않습니다."),

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
	SHOPPING_LIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "SHOPPING_LIST_010", "이미 생성된 쇼핑리스트가 있습니다."),

	// Scan
	INVALID_BARCODE(HttpStatus.BAD_REQUEST, "SCAN_001", "올바르지 않은 바코드입니다."),
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
	PURCHASE_INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "PURCHASE_005", "상품 재고가 부족합니다."),
	PURCHASE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PURCHASE_006", "본인의 구매 요청만 처리할 수 있습니다."),

	// Coupon
	COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_001", "쿠폰을 찾을 수 없습니다."),
	INVALID_COUPON_PERIOD(HttpStatus.BAD_REQUEST, "COUPON_002", "쿠폰 기간 설정이 올바르지 않습니다."),
	INVALID_COUPON_DISCOUNT_VALUE(HttpStatus.BAD_REQUEST, "COUPON_003", "쿠폰 할인 값이 올바르지 않습니다."),
	COUPON_ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "COUPON_004", "이미 비활성화된 쿠폰입니다."),
	COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "COUPON_005", "현재 사용할 수 없는 쿠폰입니다."),
	COUPON_ALREADY_DOWNLOADED(HttpStatus.BAD_REQUEST, "COUPON_006", "이미 다운로드한 쿠폰입니다."),
	USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_007", "보유 쿠폰을 찾을 수 없습니다."),
	COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "COUPON_008", "이미 사용한 쿠폰입니다."),
	COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON_009", "만료된 쿠폰입니다."),
	COUPON_CONDITION_NOT_MET(HttpStatus.BAD_REQUEST, "COUPON_010", "쿠폰 적용 조건을 만족하지 않습니다."),

	// Order
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "주문 정보를 찾을 수 없습니다."),
	ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_002", "본인의 주문만 조회할 수 있습니다."),

	// Promotion
	PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMOTION_001", "행사를 찾을 수 없습니다."),
	INVALID_PROMOTION_PERIOD(HttpStatus.BAD_REQUEST, "PROMOTION_002", "행사 기간 설정이 올바르지 않습니다."),
	INVALID_PROMOTION_BENEFIT(HttpStatus.BAD_REQUEST, "PROMOTION_003", "행사 혜택 설정이 올바르지 않습니다."),
	PROMOTION_ALREADY_ENDED(HttpStatus.BAD_REQUEST, "PROMOTION_004", "이미 종료된 행사입니다."),
	PROMOTION_PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PROMOTION_005", "이미 행사에 등록된 상품입니다."),
	PROMOTION_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROMOTION_006", "행사 상품을 찾을 수 없습니다."),
	INVALID_PROMOTION_PRODUCT(HttpStatus.BAD_REQUEST, "PROMOTION_007", "행사 상품 정보가 올바르지 않습니다."),
	PROMOTION_PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PROMOTION_008", "해당 행사에 등록된 상품이 아닙니다."),
	INVALID_PROMOTION_REQUEST(HttpStatus.BAD_REQUEST, "PROMOTION_009", "프로모션 요청 값이 올바르지 않습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}