package com.princesses7.findy.shopping.product.external.entity;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_external_mappings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductExternalMapping extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_external_mapping_id")
	private Long productExternalMappingId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "external_source", nullable = false, length = 30)
	private String externalSource;

	@Column(name = "external_product_id", nullable = false, length = 100)
	private String externalProductId;

	@Column(name = "external_product_name", length = 255)
	private String externalProductName;

	@Column(name = "external_brand_name", length = 255)
	private String externalBrandName;

	@Column(name = "match_confidence", nullable = false, precision = 5, scale = 4)
	private BigDecimal matchConfidence;

	@Enumerated(EnumType.STRING)
	@Column(name = "match_status", nullable = false, length = 30)
	private ProductExternalMatchStatus matchStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "matched_by", nullable = false, length = 30)
	private ProductExternalMatchedBy matchedBy;

	@Column(name = "match_reason", length = 500)
	private String matchReason;

	public static ProductExternalMapping create(
		Long productId,
		String externalSource,
		String externalProductId,
		String externalProductName,
		String externalBrandName,
		BigDecimal matchConfidence,
		ProductExternalMatchStatus matchStatus,
		ProductExternalMatchedBy matchedBy,
		String matchReason
	) {
		ProductExternalMapping mapping = new ProductExternalMapping();
		mapping.productId = productId;
		mapping.externalSource = externalSource;
		mapping.externalProductId = externalProductId;
		mapping.externalProductName = externalProductName;
		mapping.externalBrandName = externalBrandName;
		mapping.matchConfidence = matchConfidence;
		mapping.matchStatus = matchStatus;
		mapping.matchedBy = matchedBy;
		mapping.matchReason = matchReason;
		return mapping;
	}

	public void updateMatch(
		String externalProductName,
		String externalBrandName,
		BigDecimal matchConfidence,
		ProductExternalMatchStatus matchStatus,
		ProductExternalMatchedBy matchedBy,
		String matchReason
	) {
		this.externalProductName = externalProductName;
		this.externalBrandName = externalBrandName;
		this.matchConfidence = matchConfidence;
		this.matchStatus = matchStatus;
		this.matchedBy = matchedBy;
		this.matchReason = matchReason;
	}
}