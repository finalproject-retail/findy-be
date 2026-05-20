package com.princesses7.findy.shopping.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.order.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}