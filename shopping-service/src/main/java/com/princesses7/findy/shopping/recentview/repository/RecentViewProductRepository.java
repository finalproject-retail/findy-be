package com.princesses7.findy.shopping.recentview.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.recentview.entity.RecentViewProduct;

public interface RecentViewProductRepository extends JpaRepository<RecentViewProduct, Long> {

	Optional<RecentViewProduct> findByUserIdAndProductProductId(Long userId, Long productId);

	@Query("""
		SELECT recentViewProduct
		FROM RecentViewProduct recentViewProduct
		JOIN FETCH recentViewProduct.product product
		WHERE recentViewProduct.userId = :userId
		  AND product.isDeleted = false
		ORDER BY recentViewProduct.viewedAt DESC, recentViewProduct.recentViewId DESC
		""")
	List<RecentViewProduct> findRecentViewProducts(
		@Param("userId") Long userId,
		Pageable pageable
	);
}