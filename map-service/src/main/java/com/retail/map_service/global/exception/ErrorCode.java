package com.retail.map_service.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "요청 값이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "접근 권한이 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_001", "매장을 찾을 수 없습니다."),
    STORE_MAP_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_004", "매장 지도를 찾을 수 없습니다."),
    GRID_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_002", "격자를 찾을 수 없습니다."),
    PATH_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_005", "경로를 찾을 수 없습니다."),
    PATH_START_GRID_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_008", "매장 입구(START) 격자가 설정되지 않았습니다."),
    PATH_CURRENT_NOT_NAVIGABLE(HttpStatus.BAD_REQUEST, "MAP_006", "현재 위치 격자에서 통로로 진입할 수 없습니다."),
    PATH_DESTINATION_NOT_NAVIGABLE(HttpStatus.BAD_REQUEST, "MAP_007", "목적지 격자에서 통로를 찾을 수 없습니다."),
    BEACON_NOT_FOUND(HttpStatus.NOT_FOUND, "MAP_003", "비콘을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
