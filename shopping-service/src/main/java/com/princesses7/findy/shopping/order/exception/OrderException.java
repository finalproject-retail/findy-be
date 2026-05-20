package com.princesses7.findy.shopping.order.exception;

import com.princesses7.findy.shopping.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public class OrderException extends RuntimeException {

	private final ErrorCode errorCode;

	public OrderException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}