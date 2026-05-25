package com.retail.map_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.StoreEntity;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
}
