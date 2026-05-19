package com.princesses7.findy.shopping.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.cart.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}