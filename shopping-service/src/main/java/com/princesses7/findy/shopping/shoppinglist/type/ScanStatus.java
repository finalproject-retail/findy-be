package com.princesses7.findy.shopping.shoppinglist.type;

/*
	scanStatus === "SCANNED"이면 취소선
	scanStatus === "PARTIALLY_SCANNED"이면 부분 스캔으로 표시
	scanStatus === "NOT_SCANNED"이면 기본 표시
 */

public enum ScanStatus {
	NOT_SCANNED,
	PARTIALLY_SCANNED,
	SCANNED
}