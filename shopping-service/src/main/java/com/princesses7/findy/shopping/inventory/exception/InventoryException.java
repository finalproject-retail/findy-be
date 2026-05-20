package com.princesses7.findy.shopping.inventory.exception;

import com.princesses7.findy.shopping.global.exception.ErrorCode;

import lombok.Getter;

@Getter
public class InventoryException extends RuntimeException {

	private final ErrorCode errorCode;

	public InventoryException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}