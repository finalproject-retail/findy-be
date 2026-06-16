package com.princesses7.findy.recommendation.product.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(schema = "shopping_service", name = "products")
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

	@Column(name = "description")
	private String description;

	@Column(name = "image_url")
	private String imageUrl;

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

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public boolean isRecommendable() {
		return deletedAt == null
			&& !"SOLD_OUT".equals(saleStatus)
			&& !"DISCONTINUED".equals(saleStatus);
	}

	@Transient
	public boolean isDeleted() {
		return deletedAt != null;
	}

	public String toEmbeddingText(String categoryName) {
		return """
			마트 상품 정보입니다.
			
			상품명: %s
			브랜드: %s
			카테고리: %s
			상품 설명: %s
			판매 단위: %s
			중량/용량: %s
			알레르기 정보: %s
			상품 배지: %s
			
			이 텍스트는 상품의 의미 기반 추천과 검색을 위한 임베딩 생성에 사용됩니다.
			상품명, 카테고리, 설명, 판매 단위, 용량 정보를 종합해 상품의 용도와 성격을 표현합니다.
			""".formatted(
			nullToEmpty(productName),
			nullToEmpty(brandName),
			nullToEmpty(categoryName),
			nullToEmpty(description),
			nullToEmpty(salesUnit),
			nullToEmpty(volume),
			nullToEmpty(allergyInfo),
			nullToEmpty(badgeText)
		);
	}

	public String toRelatedRecommendationText(String categoryName) {
		return """
			오프라인 대형마트의 연관 상품 추천 요청입니다.
			
			사용자가 현재 보고 있거나 구매하려는 기준 상품:
			상품명: %s
			브랜드: %s
			카테고리: %s
			상품 설명: %s
			판매 단위: %s
			중량/용량: %s
			알레르기 정보: %s
			상품 배지: %s
			
			추천 목표:
			이 기준 상품을 대체하는 비슷한 상품이 아니라,
			함께 구매하면 좋은 보완 상품, 곁들임 상품, 함께 소비되는 상품,
			같은 쇼핑 목적에서 같이 담기 쉬운 상품을 찾습니다.
			""".formatted(
			nullToEmpty(productName),
			nullToEmpty(brandName),
			nullToEmpty(categoryName),
			nullToEmpty(description),
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