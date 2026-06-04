package com.princesses7.findy.shopping.product.external.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.product.external.entity.ProductExternalPrice;

public interface ProductExternalPriceRepository extends JpaRepository<ProductExternalPrice, Long> {

	boolean existsByExternalSourceAndExternalProductIdAndExternalStoreIdAndInspectedDate(
		String externalSource,
		String externalProductId,
		String externalStoreId,
		LocalDate inspectedDate
	);
}