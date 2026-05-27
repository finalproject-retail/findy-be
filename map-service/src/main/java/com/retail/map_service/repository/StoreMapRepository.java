package com.retail.map_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.StoreMapEntity;

public interface StoreMapRepository extends JpaRepository<StoreMapEntity, Long> {

	Optional<StoreMapEntity> findFirstByStore_StoreIdOrderByMapIdAsc(Long storeId);
}