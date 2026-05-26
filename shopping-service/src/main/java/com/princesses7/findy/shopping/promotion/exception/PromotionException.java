package com.princesses7.findy.shopping.promotion.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class PromotionException extends BaseException {

	public PromotionException(ErrorCode errorCode) {
		super(errorCode);
	}
}