package com.princesses7.findy.user.global.exception;

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

	// User
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 회원입니다."),
	DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 가입된 이메일입니다."),
	DUPLICATED_PHONE_NUMBER(HttpStatus.CONFLICT, "USER_003", "이미 등록된 전화번호입니다."),
	INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "USER_004", "유효하지 않은 회원 상태입니다."),
	DELETED_USER(HttpStatus.BAD_REQUEST, "USER_005", "탈퇴한 회원입니다."),
	USER_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "USER_006", "이미 탈퇴 처리된 회원입니다."),
	INVALID_USER_INFO(HttpStatus.BAD_REQUEST, "USER_007", "회원 정보가 올바르지 않습니다."),

	// Auth
	INVALID_LOGIN_INFO(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 일치하지 않습니다."),
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "AUTH_002", "비밀번호가 올바르지 않습니다."),
	PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH_003", "비밀번호가 일치하지 않습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "만료된 토큰입니다."),
	UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_006", "지원하지 않는 토큰입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_007", "접근 권한이 없습니다."),

	// Social
	SOCIAL_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "SOCIAL_001", "소셜 인증에 실패했습니다."),
	UNSUPPORTED_SOCIAL_PROVIDER(HttpStatus.BAD_REQUEST, "SOCIAL_002", "지원하지 않는 소셜 로그인 제공자입니다."),
	SOCIAL_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "SOCIAL_003", "연결된 소셜 계정이 없습니다."),
	SOCIAL_ACCOUNT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "SOCIAL_004", "이미 연결된 소셜 계정입니다."),
	SOCIAL_EMAIL_NOT_PROVIDED(HttpStatus.BAD_REQUEST, "SOCIAL_005", "소셜 계정 이메일을 확인할 수 없습니다."),
	SOCIAL_CLIENT_NOT_CONFIGURED(HttpStatus.INTERNAL_SERVER_ERROR, "SOCIAL_006", "소셜 로그인 설정이 필요합니다."),

	// Email Verification
	EMAIL_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "EMAIL_001", "이메일 인증 요청을 찾을 수 없습니다."),
	EMAIL_VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "EMAIL_002", "인증 코드가 일치하지 않습니다."),
	EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_003", "인증 코드가 만료되었습니다."),
	EMAIL_VERIFICATION_REQUIRED(HttpStatus.BAD_REQUEST, "EMAIL_004", "이메일 인증이 필요합니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_005", "인증 메일 발송에 실패했습니다."),

	// User Grade
	USER_GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "GRADE_001", "존재하지 않는 회원 등급입니다."),
	INVALID_USER_GRADE(HttpStatus.BAD_REQUEST, "GRADE_002", "유효하지 않은 회원 등급입니다."),

	// Preference
	PREFERENCE_NOT_FOUND(HttpStatus.NOT_FOUND, "PREFERENCE_001", "회원 선호 정보가 존재하지 않습니다."),
	PREFERENCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "PREFERENCE_002", "이미 선호 정보가 등록된 회원입니다."),
	PREFERENCE_REQUIRED(HttpStatus.BAD_REQUEST, "PREFERENCE_003", "선호 정보 입력이 필요합니다."),
	INVALID_PREFERENCE_CATEGORY(HttpStatus.BAD_REQUEST, "PREFERENCE_004", "유효하지 않은 선호 카테고리입니다."),
	INVALID_SHOPPING_STYLE(HttpStatus.BAD_REQUEST, "PREFERENCE_005", "유효하지 않은 쇼핑 스타일입니다."),
	EMPTY_PREFERENCE_CATEGORY(HttpStatus.BAD_REQUEST, "PREFERENCE_006", "선호 카테고리를 하나 이상 선택해야 합니다."),
	EMPTY_SHOPPING_STYLE(HttpStatus.BAD_REQUEST, "PREFERENCE_007", "쇼핑 스타일을 하나 이상 선택해야 합니다."),
	FIRST_LOGIN_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "PREFERENCE_008", "이미 첫 로그인 설문을 완료한 회원입니다."),

	// Reward
	INSUFFICIENT_REWARD_BALANCE(HttpStatus.BAD_REQUEST, "REWARD_001", "보유 포인트가 부족합니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
}
