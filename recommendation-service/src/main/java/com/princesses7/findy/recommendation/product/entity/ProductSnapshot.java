package com.princesses7.findy.recommendation.product.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSnapshot {

	@Id
	@Column(name = "product_id")
	private Long productId;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;

	@Column(name = "brand_name")
	private String brandName;

	@Column(name = "product_name", nullable = false)
	private String productName;

	@Column(name = "original_price", nullable = false)
	private Integer originalPrice;

	@Column(name = "sale_price", nullable = false)
	private Integer salePrice;

	@Column(name = "discount_rate", nullable = false)
	private BigDecimal discountRate;

	@Column(name = "description")
	private String description;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "packaging_type")
	private String packagingType;

	@Column(name = "sales_unit")
	private String salesUnit;

	@Column(name = "volume")
	private String volume;

	@Column(name = "allergy_info")
	private String allergyInfo;

	@Column(name = "badge_text")
	private String badgeText;

	@Column(name = "sale_status", nullable = false)
	private String saleStatus;

	@Column(name = "is_deleted", nullable = false)
	private boolean deleted;

	public boolean isRecommendable() {
		return !deleted
			&& !"SOLD_OUT".equals(saleStatus)
			&& !"DISCONTINUED".equals(saleStatus);
	}

	public String toEmbeddingText(String categoryName) {
		return """
			마트 상품 정보입니다.
			상품명: %s
			브랜드: %s
			카테고리: %s
			상품 설명: %s
			포장 타입: %s
			판매 단위: %s
			중량/용량: %s
			알레르기 정보: %s
			상품 배지: %s
			""".formatted(
			nullToEmpty(productName),
			nullToEmpty(brandName),
			nullToEmpty(categoryName),
			nullToEmpty(description),
			nullToEmpty(packagingType),
			nullToEmpty(salesUnit),
			nullToEmpty(volume),
			nullToEmpty(allergyInfo),
			nullToEmpty(badgeText)
		);
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}