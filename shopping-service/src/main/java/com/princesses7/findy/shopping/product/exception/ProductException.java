package com.princesses7.findy.shopping.product.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class ProductException extends BaseException {

	public ProductException(ErrorCode errorCode) {
		super(errorCode);
	}
}