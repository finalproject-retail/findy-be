package com.princesses7.findy.shopping.inventory.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.princesses7.findy.shopping.inventory.entity.Inventory;

import jakarta.persistence.LockModeType;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	boolean existsByProductProductIdAndStoreId(Long productId, Long storeId);

	Optional<Inventory> findByProductProductIdAndStoreId(Long productId, Long storeId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Inventory> findAllByProductProductIdInAndStoreId(
		Collection<Long> productIds,
		Long storeId
	);
}