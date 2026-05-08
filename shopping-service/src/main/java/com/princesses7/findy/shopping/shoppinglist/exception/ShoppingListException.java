package com.princesses7.findy.shopping.shoppinglist.exception;

import com.princesses7.findy.shopping.global.exception.BaseException;
import com.princesses7.findy.shopping.global.exception.ErrorCode;

public class ShoppingListException extends BaseException {

	public ShoppingListException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ShoppingListException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}
}