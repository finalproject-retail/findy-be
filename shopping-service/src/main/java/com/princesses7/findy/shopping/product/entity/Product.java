package com.princesses7.findy.shopping.product.entity;

import java.math.BigDecimal;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;

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
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_id")
	private Long productId;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;

	@Column(name = "brand_name", length = 100)
	private String brandName;

	@Column(name = "product_name", nullable = false, length = 255)
	private String productName;

	@Column(name = "barcode", length = 100, unique = true)
	private String barcode;

	@Column(name = "external_source", length = 50)
	private String externalSource; // MFDS, NAVER

	@Column(name = "external_product_id", length = 100)
	private String externalProductId; // 품목보고번호 또는 네이버 productId

	@Column(name = "original_price", nullable = false)
	private Integer originalPrice;

	@Column(name = "sale_price", nullable = false)
	private Integer salePrice;

	@Column(name = "discount_rate", nullable = false, precision = 5, scale = 2)
	private BigDecimal discountRate;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

	@Column(name = "packaging_type", length = 100)
	private String packagingType;

	@Column(name = "sales_unit", length = 100)
	private String salesUnit;

	@Column(name = "volume", length = 100)
	private String volume;

	@Column(name = "allergy_info", columnDefinition = "TEXT")
	private String allergyInfo;

	@Column(name = "badge_text", length = 100)
	private String badgeText;

	@Column(name = "category_confidence", precision = 5, scale = 2)
	private BigDecimal categoryConfidence;

	@Column(name = "category_classified_by", length = 30)
	private String categoryClassifiedBy;

	@Column(name = "category_review_required", nullable = false)
	private Boolean categoryReviewRequired = false;

	@Enumerated(EnumType.STRING)
	@Column(name = "sale_status", nullable = false, length = 30)
	private SaleStatus saleStatus;

	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;

	public static Product create(ProductImportCommand command) {
		Product product = new Product();
		product.categoryId = command.categoryId();
		product.brandName = command.brandName();
		product.productName = command.productName();
		product.barcode = command.barcode();
		product.externalSource = command.externalSource();
		product.externalProductId = command.externalProductId();
		product.originalPrice = command.originalPrice();
		product.salePrice = command.salePrice();
		product.discountRate = command.discountRate();
		product.description = command.description();
		product.imageUrl = command.imageUrl();
		product.packagingType = command.packagingType();
		product.salesUnit = command.salesUnit();
		product.volume = command.volume();
		product.allergyInfo = command.allergyInfo();
		product.badgeText = command.badgeText();
		product.saleStatus = command.saleStatus();

		product.categoryConfidence = command.categoryConfidence();
		product.categoryClassifiedBy = command.categoryClassifiedBy();
		product.categoryReviewRequired = command.categoryReviewRequired();
		
		product.isDeleted = false;
		return product;
	}
}
