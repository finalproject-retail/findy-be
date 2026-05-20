package com.princesses7.findy.shopping.order.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class OrderException extends BaseException {

	public OrderException(ErrorCode errorCode) {
		super(errorCode);
	}

	public OrderException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}