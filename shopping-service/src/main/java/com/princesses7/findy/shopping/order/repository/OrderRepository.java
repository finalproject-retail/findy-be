package com.princesses7.findy.shopping.order.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}