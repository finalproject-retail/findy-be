package com.retail.map_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.retail.map_service.entity.BeaconEntity;

public interface BeaconRepository extends JpaRepository<BeaconEntity, Long> {

	Optional<BeaconEntity> findByStore_StoreIdAndGrid_GridId(Long storeId, Long gridId);

	@Query("""
			SELECT b FROM BeaconEntity b
			JOIN FETCH b.grid g
			WHERE b.store.storeId = :storeId
			ORDER BY b.beaconId ASC
			""")
	List<BeaconEntity> findAllByStoreIdWithGrid(@Param("storeId") Long storeId);
}
