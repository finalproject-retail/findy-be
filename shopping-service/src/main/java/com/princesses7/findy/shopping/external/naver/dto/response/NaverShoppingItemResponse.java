package com.princesses7.findy.shopping.external.naver.dto.response;

public record NaverShoppingItemResponse(
	String title,
	String link,
	String image,
	String lprice,
	String hprice,
	String mallName,
	String productId,
	String productType,
	String brand,
	String maker,
	String category1,
	String category2,
	String category3,
	String category4
) {
}