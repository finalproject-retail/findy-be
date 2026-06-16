package com.princesses7.findy.recommendation.inventory.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.inventory.entity.InventorySnapshot;

public interface InventorySnapshotRepository extends JpaRepository<InventorySnapshot, Long> {

	@Query(value = """
		SELECT
			i.inventory_id,
			i.product_id,
			i.store_id,
			i.stock_quantity,
			i.unit,
			CASE
				WHEN i.stock_quantity IS NULL OR i.stock_quantity <= 0 THEN 'OUT_OF_STOCK'
				WHEN i.stock_quantity <= 5 THEN 'LOW_STOCK'
				ELSE 'IN_STOCK'
			END AS stock_status
		FROM shopping_service.inventories i
		WHERE i.store_id = :storeId
			AND i.product_id IN (:productIds)
		""", nativeQuery = true)
	List<InventorySnapshot> findByStoreIdAndProductIdIn(
		@Param("storeId") Long storeId,
		@Param("productIds") Collection<Long> productIds
	);

	@Query(value = """
		SELECT
			i.inventory_id,
			i.product_id,
			i.store_id,
			i.stock_quantity,
			i.unit,
			CASE
				WHEN i.stock_quantity IS NULL OR i.stock_quantity <= 0 THEN 'OUT_OF_STOCK'
				WHEN i.stock_quantity <= 5 THEN 'LOW_STOCK'
				ELSE 'IN_STOCK'
			END AS stock_status
		FROM shopping_service.inventories i
		WHERE i.product_id = :productId
			AND i.store_id = :storeId
		LIMIT 1
		""", nativeQuery = true)
	Optional<InventorySnapshot> findByProductIdAndStoreId(
		@Param("productId") Long productId,
		@Param("storeId") Long storeId
	);
}