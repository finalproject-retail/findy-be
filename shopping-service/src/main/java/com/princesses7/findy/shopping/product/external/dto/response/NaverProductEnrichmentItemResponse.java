package com.princesses7.findy.shopping.product.external.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.princesses7.findy.shopping.external.naver.dto.response.NaverShoppingItemResponse;
import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.external.entity.ProductExternalMatchStatus;

public record NaverProductEnrichmentItemResponse(
	Long productId,
	String productName,
	Integer beforePrice,
	Integer afterPrice,
	String beforeImageUrl,
	String afterImageUrl,
	String naverProductId,
	String naverTitle,
	String naverImageUrl,
	String naverPrice,
	String matchStatus,
	BigDecimal matchConfidence,
	boolean applied,
	List<String> updatedFields,
	String message
) {

	public static NaverProductEnrichmentItemResponse applied(
		Product product,
		Integer beforePrice,
		String beforeImageUrl,
		NaverShoppingItemResponse item,
		ProductExternalMatchStatus status,
		BigDecimal confidence,
		List<String> updatedFields,
		String cleanTitle
	) {
		return new NaverProductEnrichmentItemResponse(
			product.getProductId(),
			product.getProductName(),
			beforePrice,
			product.getOriginalPrice(),
			beforeImageUrl,
			product.getImageUrl(),
			item.productId(),
			cleanTitle,
			item.image(),
			item.lprice(),
			status.name(),
			confidence,
			true,
			updatedFields,
			"네이버 상품 정보 보강 완료"
		);
	}

	public static NaverProductEnrichmentItemResponse reviewRequired(
		Product product,
		NaverShoppingItemResponse item,
		ProductExternalMatchStatus status,
		BigDecimal confidence,
		String cleanTitle
	) {
		return new NaverProductEnrichmentItemResponse(
			product.getProductId(),
			product.getProductName(),
			product.getOriginalPrice(),
			product.getOriginalPrice(),
			product.getImageUrl(),
			product.getImageUrl(),
			item.productId(),
			cleanTitle,
			item.image(),
			item.lprice(),
			status.name(),
			confidence,
			false,
			List.of(),
			"검토 필요"
		);
	}

	public static NaverProductEnrichmentItemResponse skipped(
		Product product,
		String message
	) {
		return new NaverProductEnrichmentItemResponse(
			product.getProductId(),
			product.getProductName(),
			product.getOriginalPrice(),
			product.getOriginalPrice(),
			product.getImageUrl(),
			product.getImageUrl(),
			null,
			null,
			null,
			null,
			ProductExternalMatchStatus.REJECTED.name(),
			BigDecimal.ZERO,
			false,
			List.of(),
			message
		);
	}
}
