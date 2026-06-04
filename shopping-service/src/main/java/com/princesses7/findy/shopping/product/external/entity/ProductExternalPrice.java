package com.princesses7.findy.shopping.product.external.entity;

import java.time.LocalDate;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "product_external_prices")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductExternalPrice extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_external_price_id")
	private Long productExternalPriceId;

	@Column(name = "product_id", nullable = false)
	private Long productId;

	@Column(name = "external_source", nullable = false, length = 30)
	private String externalSource;

	@Column(name = "external_product_id", nullable = false, length = 100)
	private String externalProductId;

	@Column(name = "external_store_id", length = 100)
	private String externalStoreId;

	@Column(name = "price", nullable = false)
	private Integer price;

	@Column(name = "inspected_date", nullable = false)
	private LocalDate inspectedDate;

	public static ProductExternalPrice create(
		Long productId,
		String externalSource,
		String externalProductId,
		String externalStoreId,
		Integer price,
		LocalDate inspectedDate
	) {
		ProductExternalPrice externalPrice = new ProductExternalPrice();
		externalPrice.productId = productId;
		externalPrice.externalSource = externalSource;
		externalPrice.externalProductId = externalProductId;
		externalPrice.externalStoreId = externalStoreId;
		externalPrice.price = price;
		externalPrice.inspectedDate = inspectedDate;
		return externalPrice;
	}
}