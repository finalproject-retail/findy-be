package com.princesses7.findy.shopping.product.external.dto.response;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.external.kca.dto.response.KcaProductPriceItemResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchStatus;

public record KcaMissingPriceSyncItemResponse(
	Long productId,
	String productName,
	Integer beforePrice,
	Integer afterPrice,
	String kcaGoodId,
	String kcaGoodName,
	String kcaBrandName,
	String entpId,
	String kcaPrice,
	String matchStatus,
	BigDecimal matchConfidence,
	boolean applied,
	String message
) {

	public static KcaMissingPriceSyncItemResponse applied(
		Product product,
		Integer beforePrice,
		Integer afterPrice,
		KcaProductPriceItemResponse priceItem,
		ProductExternalMatchStatus status,
		BigDecimal confidence
	) {
		return new KcaMissingPriceSyncItemResponse(
			product.getProductId(),
			product.getProductName(),
			beforePrice,
			afterPrice,
			priceItem.goodId(),
			priceItem.goodName(),
			priceItem.productEntpName(),
			priceItem.entpId(),
			priceItem.goodPrice(),
			status.name(),
			confidence,
			true,
			"가격 반영 완료"
		);
	}

	public static KcaMissingPriceSyncItemResponse reviewRequired(
		Product product,
		KcaProductPriceItemResponse priceItem,
		ProductExternalMatchStatus status,
		BigDecimal confidence
	) {
		return new KcaMissingPriceSyncItemResponse(
			product.getProductId(),
			product.getProductName(),
			product.getOriginalPrice(),
			product.getOriginalPrice(),
			priceItem.goodId(),
			priceItem.goodName(),
			priceItem.productEntpName(),
			priceItem.entpId(),
			priceItem.goodPrice(),
			status.name(),
			confidence,
			false,
			"검수 필요"
		);
	}

	public static KcaMissingPriceSyncItemResponse skipped(
		KcaProductPriceItemResponse priceItem,
		String message
	) {
		return new KcaMissingPriceSyncItemResponse(
			null,
			null,
			null,
			null,
			priceItem.goodId(),
			priceItem.goodName(),
			priceItem.productEntpName(),
			priceItem.entpId(),
			priceItem.goodPrice(),
			ProductExternalMatchStatus.REJECTED.name(),
			BigDecimal.ZERO,
			false,
			message
		);
	}
}