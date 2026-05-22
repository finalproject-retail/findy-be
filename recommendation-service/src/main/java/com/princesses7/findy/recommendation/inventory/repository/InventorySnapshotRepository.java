package com.princesses7.findy.recommendation.inventory.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;

public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {

	Optional<InventorySnapshot> findByProductIdAndStoreId(
		Long productId,
		Long storeId
	);

	List<InventorySnapshot> findByStoreIdAndProductIdIn(
		Long storeId,
		Collection<Long> productIds
	);
}