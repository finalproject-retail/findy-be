package com.princesses7.findy.shopping.product.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.product.entity.Product;
import com.princesses7.findy.shopping.product.entity.SaleStatus;

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
		  AND (:keyword IS NULL
		       OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
		       OR LOWER(COALESCE(p.brandName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
		       OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
		  )
		  AND (:categoryId IS NULL OR p.categoryId = :categoryId)
		  AND (:saleStatus IS NULL OR p.saleStatus = :saleStatus)
		""")
	Page<Product> findAdminProducts(
		@Param("keyword") String keyword,
		@Param("categoryId") Long categoryId,
		@Param("saleStatus") SaleStatus saleStatus,
		Pageable pageable
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