package com.princesses7.findy.shopping.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.inventory.entity.Inventory;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	boolean existsByProductProductIdAndStoreId(Long productId, Long storeId);

	Optional<Inventory> findByProductProductIdAndStoreId(Long productId, Long storeId);
}