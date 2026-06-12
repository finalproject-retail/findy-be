package com.princesses7.findy.shopping.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.shopping.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);

	@Query("""
		SELECT DISTINCT o
		FROM Order o
		LEFT JOIN FETCH o.orderItems oi
		WHERE o.userId = :userId
		ORDER BY o.createdAt DESC
		""")
	List<Order> findAllWithItemsByUserIdOrderByCreatedAtDesc(
		@Param("userId") Long userId
	);

	@Query("""
		SELECT o
		FROM Order o
		LEFT JOIN FETCH o.orderItems oi
		WHERE o.orderId = :orderId
		""")
	Optional<Order> findByIdWithItems(
		@Param("orderId") Long orderId
	);
}