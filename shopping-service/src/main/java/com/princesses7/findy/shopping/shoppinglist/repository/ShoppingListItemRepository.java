package com.princesses7.findy.shopping.shoppinglist.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.shoppinglist.entity.ShoppingListItem;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

	List<ShoppingListItem> findAllByShoppingListShoppingListId(Long shoppingListId);

	Optional<ShoppingListItem> findByShoppingListShoppingListIdAndProductId(
		Long shoppingListId,
		Long productId
	);
}