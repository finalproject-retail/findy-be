package com.princesses7.findy.shopping.shoppinglist.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

	Optional<ShoppingList> findByCartCartId(Long cartId);

	Optional<ShoppingList> findByCartUserId(Long userId);

	boolean existsByCartCartId(Long cartId);
}