package com.princesses7.findy.shopping.coupon.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class CouponException extends BaseException {

	public CouponException(ErrorCode errorCode) {
		super(errorCode);
	}
}