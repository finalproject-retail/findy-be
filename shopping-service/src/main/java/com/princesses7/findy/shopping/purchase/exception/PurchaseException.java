package com.princesses7.findy.shopping.purchase.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class PurchaseException extends BaseException {

	public PurchaseException(ErrorCode errorCode) {
		super(errorCode);
	}
}