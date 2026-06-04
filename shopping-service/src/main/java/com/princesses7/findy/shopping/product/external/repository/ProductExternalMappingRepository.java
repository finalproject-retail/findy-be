package com.princesses7.findy.shopping.product.external.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.product.external.entity.ProductExternalMapping;

public interface ProductExternalMappingRepository extends JpaRepository<ProductExternalMapping, Long> {

	Optional<ProductExternalMapping> findByExternalSourceAndExternalProductIdAndProductId(
		String externalSource,
		String externalProductId,
		Long productId
	);
}