package com.retail.map_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.BeaconSignalLogEntity;

public interface BeaconSignalLogRepository extends JpaRepository<BeaconSignalLogEntity, Long> {
}
