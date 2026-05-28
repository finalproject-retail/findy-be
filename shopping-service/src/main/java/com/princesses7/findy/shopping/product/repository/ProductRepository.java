package com.princesses7.findy.shopping.product.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.princesses7.findy.shopping.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	List<Product> findAllByProductIdInAndIsDeletedFalse(Collection<Long> productIds);

	Page<Product> findByIsDeletedFalse(Pageable pageable);

	Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

	Page<Product> findByProductNameContainingIgnoreCaseAndIsDeletedFalse(
		String keyword,
		Pageable pageable
	);

	Page<Product> findByCategoryIdAndProductNameContainingIgnoreCaseAndIsDeletedFalse(
		Long categoryId,
		String keyword,
		Pageable pageable
	);

	Optional<Product> findByProductIdAndIsDeletedFalse(Long productId);

	boolean existsByBarcodeAndIsDeletedFalse(String barcode);

	Optional<Product> findByBarcodeAndIsDeletedFalse(String barcode);

	boolean existsByExternalSourceAndExternalProductIdAndIsDeletedFalse(
		String externalSource,
		String externalProductId
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.isDeleted = false
		  AND p.saleStatus = com.princesses7.findy.shopping.product.entity.SaleStatus.ON_SALE
		ORDER BY p.discountRate DESC, p.salePrice ASC, p.productId ASC
		""")
	List<Product> findMartRecommendedProducts(Pageable pageable);
}