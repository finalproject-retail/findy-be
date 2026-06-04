package com.retail.map_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.BeaconSignalLogEntity;

public interface BeaconSignalLogRepository extends JpaRepository<BeaconSignalLogEntity, Long> {

	Optional<BeaconSignalLogEntity> findTopByUserIdAndStore_StoreIdOrderByTimestampIsoDesc(
		Long userId,
		Long storeId
	);
}
