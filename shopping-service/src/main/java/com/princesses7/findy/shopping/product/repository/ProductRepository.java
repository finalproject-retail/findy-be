package com.princesses7.findy.shopping.product.repository;

import java.time.LocalDateTime;
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

	Page<Product> findByDeletedAtIsNullAndOriginalPriceGreaterThan(Integer originalPrice, Pageable pageable);

	Page<Product> findByCategoryIdAndDeletedAtIsNullAndOriginalPriceGreaterThan(
		Long categoryId,
		Integer originalPrice,
		Pageable pageable
	);

	Page<Product> findByProductNameContainingIgnoreCaseAndDeletedAtIsNullAndOriginalPriceGreaterThan(
		String keyword,
		Integer originalPrice,
		Pageable pageable
	);

	Page<Product> findByCategoryIdAndProductNameContainingIgnoreCaseAndDeletedAtIsNullAndOriginalPriceGreaterThan(
		Long categoryId,
		String keyword,
		Integer originalPrice,
		Pageable pageable
	);

	Optional<Product> findByProductIdAndDeletedAtIsNull(Long productId);

	Optional<Product> findByProductIdAndDeletedAtIsNullAndOriginalPriceGreaterThan(
		Long productId,
		Integer originalPrice
	);

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
		  AND (:filterByCategoryIds = false OR p.categoryId IN :categoryIds)
		  AND (:saleStatus IS NULL OR p.saleStatus = :saleStatus)
		  AND (:categoryReviewRequired IS NULL OR p.categoryReviewRequired = :categoryReviewRequired)
		""")
	Page<Product> findAdminProductsWithoutKeyword(
		@Param("categoryIds") Collection<Long> categoryIds,
		@Param("filterByCategoryIds") boolean filterByCategoryIds,
		@Param("saleStatus") SaleStatus saleStatus,
		@Param("categoryReviewRequired") Boolean categoryReviewRequired,
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
		  AND (:filterByCategoryIds = false OR p.categoryId IN :categoryIds)
		  AND (:saleStatus IS NULL OR p.saleStatus = :saleStatus)
		  AND (:categoryReviewRequired IS NULL OR p.categoryReviewRequired = :categoryReviewRequired)
		""")
	Page<Product> findAdminProductsWithKeyword(
		@Param("keyword") String keyword,
		@Param("categoryIds") Collection<Long> categoryIds,
		@Param("filterByCategoryIds") boolean filterByCategoryIds,
		@Param("saleStatus") SaleStatus saleStatus,
		@Param("categoryReviewRequired") Boolean categoryReviewRequired,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND p.saleStatus = com.princesses7.findy.shopping.product.entity.SaleStatus.ON_SALE
		  AND p.productId IN :productIds
		  AND (:categoryId IS NULL OR p.categoryId = :categoryId)
		  AND (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
		  AND EXISTS (
		  	SELECT 1
		  	FROM Inventory i
		  	WHERE i.product = p
		  	  AND i.storeId = :storeId
		  	  AND i.stockQuantity > 0
		  )
		""")
	List<Product> findPopularProductsByRedisIds(
		@Param("productIds") Collection<Long> productIds,
		@Param("categoryId") Long categoryId,
		@Param("keyword") String keyword,
		@Param("storeId") Long storeId
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND p.saleStatus = com.princesses7.findy.shopping.product.entity.SaleStatus.ON_SALE
		  AND EXISTS (
			SELECT 1
			FROM Inventory i
			WHERE i.product = p
			  AND i.storeId = :storeId
			  AND i.stockQuantity > 0
		  )
		ORDER BY p.createdAt DESC, p.productId ASC
		""")
	List<Product> findMartRecommendedProducts(
		@Param("storeId") Long storeId,
		Pageable pageable
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.productId IN :productIds
		  AND p.deletedAt IS NULL
		  AND p.originalPrice > 0
		""")
	List<Product> findAllVisibleByProductIdIn(
		@Param("productIds") Collection<Long> productIds
	);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND (
		       p.originalPrice IS NULL
		       OR p.originalPrice <= 0
		       OR p.imageUrl IS NULL
		       OR p.imageUrl = ''
		       OR p.externalProductId IS NULL
		       OR p.externalProductId = ''
		  )
		ORDER BY p.productId ASC
		""")
	List<Product> findNaverEnrichmentTargets(Pageable pageable);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND (p.originalPrice IS NULL OR p.originalPrice <= 0)
		ORDER BY p.productId ASC
		""")
	List<Product> findPriceMissingProducts(Pageable pageable);

	@Query("""
		SELECT p
		FROM Product p
		WHERE p.deletedAt IS NULL
		  AND p.originalPrice > :minPrice
		  AND p.imageUrl IS NOT NULL
		  AND TRIM(p.imageUrl) <> ''
		  AND LOWER(p.imageUrl) NOT LIKE '%findy%'
		  AND p.saleStatus <> com.princesses7.findy.shopping.product.entity.SaleStatus.SOLD_OUT
		  AND p.saleStatus <> com.princesses7.findy.shopping.product.entity.SaleStatus.DISCONTINUED
		ORDER BY p.createdAt DESC, p.productId DESC
		""")
	Page<Product> findNewDisplayableProducts(
		@Param("minPrice") Integer minPrice,
		Pageable pageable
	);

	@Query(value = """
		SELECT p.*
		FROM products p
		JOIN (
		    SELECT
		        oi.product_id,
		        SUM(oi.quantity) AS purchase_quantity,
		        COUNT(DISTINCT oi.order_id) AS order_count,
		        MAX(o.created_at) AS last_ordered_at
		    FROM order_items oi
		    JOIN orders o ON o.order_id = oi.order_id
		    WHERE o.order_status = 'COMPLETED'
		      AND o.created_at >= :fromAt
		    GROUP BY oi.product_id
		) pop ON pop.product_id = p.product_id
		JOIN inventories i
		  ON i.product_id = p.product_id
		 AND i.store_id = :storeId
		 AND i.stock_quantity > 0
		WHERE p.deleted_at IS NULL
		  AND p.sale_status = 'ON_SALE'
		ORDER BY
		  pop.purchase_quantity DESC,
		  pop.order_count DESC,
		  pop.last_ordered_at DESC,
		  p.product_id ASC
		""", nativeQuery = true)
	List<Product> findPopularProductsByPurchaseLogs(
		@Param("storeId") Long storeId,
		@Param("fromAt") LocalDateTime fromAt,
		Pageable pageable
	);
}