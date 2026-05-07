package com.princesses7.findy.shopping.shoppinglist.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingList;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {

	List<ShoppingList> findAllByCartUserId(Long userId);
}