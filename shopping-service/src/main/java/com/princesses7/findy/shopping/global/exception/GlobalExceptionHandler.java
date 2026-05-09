package com.princesses7.findy.shopping.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.princesses7.findy.shopping.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BaseException.class)
	public ResponseEntity<ApiResponse<Void>> handleBaseException(
		BaseException exception,
		HttpServletRequest request
	) {
		ErrorCode errorCode = exception.getErrorCode();

		log.warn(
			"Business exception occurred. uri={}, code={}, message={}",
			request.getRequestURI(),
			errorCode.getCode(),
			exception.getMessage()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));

		log.warn(
			"Validation exception occurred. uri={}, message={}",
			request.getRequestURI(),
			message
		);

		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), message));
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBindException(
		org.springframework.validation.BindException exception,
		HttpServletRequest request
	) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));

		log.warn(
			"Bind exception occurred. uri={}, message={}",
			request.getRequestURI(),
			message
		);

		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), message));
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException exception,
		HttpServletRequest request
	) {
		ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

		log.warn(
			"Method not allowed. uri={}, method={}",
			request.getRequestURI(),
			exception.getMethod()
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(
		Exception exception,
		HttpServletRequest request
	) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

		log.error(
			"Unexpected exception occurred. uri={}, message={}",
			request.getRequestURI(),
			exception.getMessage(),
			exception
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), errorCode.getMessage()));
	}
}