package com.princesses7.findy.shopping.product.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;
import com.princesses7.findy.shopping.product.dto.command.ProductImportCommand;
import com.princesses7.findy.shopping.product.support.CategoryGridIdResolver;

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

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "image_url", length = 500)
	private String imageUrl;

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

	@Column(name = "grid_id")
	private Long gridId;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	public static Product create(ProductImportCommand command) {
		Product product = new Product();
		product.categoryId = command.categoryId();
		product.brandName = command.brandName();
		product.productName = command.productName();
		product.barcode = command.barcode();
		product.externalSource = command.externalSource();
		product.externalProductId = command.externalProductId();
		product.originalPrice = command.originalPrice();
		product.description = command.description();
		product.imageUrl = command.imageUrl();
		product.salesUnit = command.salesUnit();
		product.volume = command.volume();
		product.allergyInfo = command.allergyInfo();
		product.badgeText = command.badgeText();
		product.saleStatus = command.saleStatus();

		product.categoryConfidence = command.categoryConfidence();
		product.categoryClassifiedBy = command.categoryClassifiedBy();
		product.categoryReviewRequired = command.categoryReviewRequired();
		product.gridId = CategoryGridIdResolver.resolveRandomGridId(command.categoryId());

		product.deletedAt = null;
		return product;
	}

	public List<String> enrichMissingFields(ProductImportCommand command, boolean canApplyBarcode) {
		List<String> updatedFields = new ArrayList<>();

		if (canApplyBarcode && isBlank(this.barcode) && hasText(command.barcode())) {
			this.barcode = command.barcode();
			updatedFields.add("barcode");
		}

		if (isBlank(this.externalSource) && hasText(command.externalSource())) {
			this.externalSource = command.externalSource();
			updatedFields.add("externalSource");
		}

		if (isBlank(this.externalProductId) && hasText(command.externalProductId())) {
			this.externalProductId = command.externalProductId();
			updatedFields.add("externalProductId");
		}

		if (isBlank(this.brandName) && hasText(command.brandName())) {
			this.brandName = command.brandName();
			updatedFields.add("brandName");
		}

		if (isBlank(this.description) && hasText(command.description())) {
			this.description = command.description();
			updatedFields.add("description");
		}

		if (isBlank(this.imageUrl) && hasText(command.imageUrl())) {
			this.imageUrl = command.imageUrl();
			updatedFields.add("imageUrl");
		}

		if (isBlank(this.salesUnit) && hasText(command.salesUnit())) {
			this.salesUnit = command.salesUnit();
			updatedFields.add("salesUnit");
		}

		if (isBlank(this.volume) && hasText(command.volume())) {
			this.volume = command.volume();
			updatedFields.add("volume");
		}

		if (isBlank(this.allergyInfo) && hasText(command.allergyInfo())) {
			this.allergyInfo = command.allergyInfo();
			updatedFields.add("allergyInfo");
		}

		if (isBlank(this.badgeText) && hasText(command.badgeText())) {
			this.badgeText = command.badgeText();
			updatedFields.add("badgeText");
		}

		if (this.categoryConfidence == null && command.categoryConfidence() != null) {
			this.categoryConfidence = command.categoryConfidence();
			updatedFields.add("categoryConfidence");
		}

		if (isBlank(this.categoryClassifiedBy) && hasText(command.categoryClassifiedBy())) {
			this.categoryClassifiedBy = command.categoryClassifiedBy();
			updatedFields.add("categoryClassifiedBy");
		}

		if (this.categoryReviewRequired == null && command.categoryReviewRequired() != null) {
			this.categoryReviewRequired = command.categoryReviewRequired();
			updatedFields.add("categoryReviewRequired");
		}

		return updatedFields;
	}

	public boolean isPriceMissing() {
		return originalPrice == null || originalPrice <= 0;
	}

	public boolean applyExternalPriceIfMissing(Integer price) {
		if (!isPriceMissing() || price == null || price <= 0) {
			return false;
		}

		this.originalPrice = price;
		return true;
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
