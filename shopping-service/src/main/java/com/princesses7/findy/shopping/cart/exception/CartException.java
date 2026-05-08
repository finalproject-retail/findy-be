package com.princesses7.findy.shopping.cart.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class CartException extends BaseException {

	public CartException(ErrorCode errorCode) {
		super(errorCode);
	}

	public CartException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}