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

	List<Product> findAllByProductIdInAndDeletedAtIsNull(Collection<Long> productIds);

	Page<Product> findByDeletedAtIsNull(Pageable pageable);

	Page<Product> findByCategoryIdAndDeletedAtIsNull(Long categoryId, Pageable pageable);

	Page<Product> findByProductNameContainingIgnoreCaseAndDeletedAtIsNull(
		String keyword,
		Pageable pageable
	);

	Page<Product> findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNull(
		Long categoryId,
		String keyword,
		Pageable pageable
	);

	Optional<Product> findByProductIdAndDeletedAtIsNull(Long productId);

	boolean existsByBarcodeAndDeletedAtIsNull(String barcode);

	Optional<Product> findByBarcodeAndDeletedAtIsNull(String barcode);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND REPLACE(LOWER(p.productName), ' ', '') = :normalizedProductName
		ORDER BY p.productId ASC
		""")
	List<Product> findAllByNormalizedProductName(
		@Param("normalizedProductName") String normalizedProductName
	);

	boolean existsByExternalSourceAndExternalProductIdAndDeletedAtIsNull(
		String externalSource,
		String externalProductId
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND (:categoryId IS NULL OR p.categoryId = :categoryId)
		  AND (:saleStatus IS NULL OR p.saleStatus = :saleStatus)
		""")
	Page<Product> findAdminProductsWithoutKeyword(
		@Param("categoryId") Long categoryId,
		@Param("saleStatus") SaleStatus saleStatus,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND (
		       LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
		       OR LOWER(COALESCE(p.brandName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
		       OR LOWER(COALESCE(p.barcode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
		  )
		  AND (:categoryId IS NULL OR p.categoryId = :categoryId)
		  AND (:saleStatus IS NULL OR p.saleStatus = :saleStatus)
		""")
	Page<Product> findAdminProductsWithKeyword(
		@Param("keyword") String keyword,
		@Param("categoryId") Long categoryId,
		@Param("saleStatus") SaleStatus saleStatus,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND p.saleStatus = com.princesses7.findy.shopping.product.entity.SaleStatus.ON_SALE
		ORDER BY p.createdAt DESC, p.productId ASC
		""")
	List<Product> findMartRecommendedProducts(Pageable pageable);
}