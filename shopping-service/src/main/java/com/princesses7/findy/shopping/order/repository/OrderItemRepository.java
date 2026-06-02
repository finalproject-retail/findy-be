package com.princesses7.findy.shopping.order.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.order.entity.OrderItem;
import com.princesses7.findy.shopping.order.repository.projection.FrequentPurchaseProductRow;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	@Query("""
		SELECT new com.princesses7.findy.shopping.order.repository.projection.FrequentPurchaseProductRow(
			p.productId,
			p.categoryId,
			p.brandName,
			p.productName,
			p.imageUrl,
			p.originalPrice,
			p.salePrice,
			p.discountRate,
			COUNT(DISTINCT o.orderId),
			SUM(oi.quantity),
			MAX(o.createdAt)
		)
		FROM OrderItem oi
		JOIN oi.order o
		JOIN Product p ON p.productId = oi.productId
		WHERE o.userId = :userId
		  AND o.orderStatus = com.princesses7.findy.shopping.order.entity.OrderStatus.COMPLETED
		  AND o.createdAt >= :fromDateTime
		  AND o.createdAt < :toDateTime
		  AND p.isDeleted = false
		GROUP BY p.productId, p.categoryId, p.brandName, p.productName, p.imageUrl,
		         p.originalPrice, p.salePrice, p.discountRate
		ORDER BY COUNT(DISTINCT o.orderId) DESC,
		         SUM(oi.quantity) DESC,
		         MAX(o.createdAt) DESC,
		         p.productId ASC
		""")
	List<FrequentPurchaseProductRow> findFrequentPurchaseProducts(
		@Param("userId") Long userId,
		@Param("fromDateTime") LocalDateTime fromDateTime,
		@Param("toDateTime") LocalDateTime toDateTime,
		Pageable pageable
	);
}