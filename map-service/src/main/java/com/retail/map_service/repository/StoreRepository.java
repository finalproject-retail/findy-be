package com.retail.map_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.StoreEntity;
import com.retail.map_service.entity.StoreStatus;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

	List<StoreEntity> findByStatusOrderByStoreIdAsc(StoreStatus status);
}
