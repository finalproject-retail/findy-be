package com.princesses7.findy.shopping.cart.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	List<CartItem> findAllByCartUserIdAndProductIdIn(
		Long userId,
		Collection<Long> productIds
	);
}