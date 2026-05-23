package com.retail.map_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.BeaconEntity;

public interface BeaconRepository extends JpaRepository<BeaconEntity, Long> {

	Optional<BeaconEntity> findByStore_StoreIdAndGrid_GridId(Long storeId, Long gridId);
}
