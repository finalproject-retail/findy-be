package com.princesses7.findy.product.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "?붿껌 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_002", "?섎せ???붿껌?낅땲??"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "?몄쬆???꾩슂?⑸땲??"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "?묎렐 沅뚰븳???놁뒿?덈떎."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "吏?먰븯吏 ?딅뒗 HTTP 硫붿꽌?쒖엯?덈떎."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "?쒕쾭 ?대? ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "?곹뭹??李얠쓣 ???놁뒿?덈떎."),
    INVALID_PRODUCT_STATUS(HttpStatus.BAD_REQUEST, "PR002", "?곹뭹 ?곹깭媛 ?щ컮瑜댁? ?딆뒿?덈떎."),
    PRODUCT_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PR003", "?곹뭹 ?꾩튂 ?뺣낫媛 ?놁뒿?덈떎."),
    PRODUCT_STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "PR004", "?곹뭹 ?ш퀬 ?뺣낫瑜?李얠쓣 ???놁뒿?덈떎."),
    INVALID_SEARCH_KEYWORD(HttpStatus.BAD_REQUEST, "PR005", "寃?됱뼱媛 ?щ컮瑜댁? ?딆뒿?덈떎."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PR006", "移댄뀒怨좊━瑜?李얠쓣 ???놁뒿?덈떎."),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "PR007", "吏?먰븯吏 ?딅뒗 ?뺣젹 湲곗??낅땲??"),
    DUPLICATE_BARCODE(HttpStatus.CONFLICT, "PR008", "?대? ?깅줉??諛붿퐫?쒖엯?덈떎."),
    BARCODE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR009", "?깅줉?섏? ?딆? 諛붿퐫?쒖엯?덈떎."),
    SEARCH_LOG_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PR010", "寃??濡쒓렇 ??μ뿉 ?ㅽ뙣?덉뒿?덈떎.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}