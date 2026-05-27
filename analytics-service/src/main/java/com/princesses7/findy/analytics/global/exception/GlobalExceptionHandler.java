package com.princesses7.findy.recommendation.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.princesses7.findy.recommendation.global.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
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

		return badRequest(request, "Validation exception occurred", message);
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiResponse<Void>> handleBindException(
		BindException exception,
		HttpServletRequest request
	) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));

		return badRequest(request, "Bind exception occurred", message);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException exception,
		HttpServletRequest request
	) {
		String message = exception.getParameterName() + "은(는) 필수 요청 파라미터입니다.";

		return badRequest(request, "Missing request parameter", message);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException exception,
		HttpServletRequest request
	) {
		String message = exception.getName() + " 값의 형식이 올바르지 않습니다.";

		return badRequest(request, "Type mismatch exception occurred", message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
		ConstraintViolationException exception,
		HttpServletRequest request
	) {
		String message = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.collect(Collectors.joining(", "));

		return badRequest(request, "Constraint violation exception occurred", message);
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

	private ResponseEntity<ApiResponse<Void>> badRequest(
		HttpServletRequest request,
		String logMessage,
		String responseMessage
	) {
		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

		log.warn(
			"{}. uri={}, message={}",
			logMessage,
			request.getRequestURI(),
			responseMessage
		);

		return ResponseEntity
			.status(errorCode.getStatus())
			.body(ApiResponse.fail(errorCode.getCode(), responseMessage));
	}
}