package com.retail.map_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.retail.map_service.entity.GridEntity;

public interface GridRepository extends JpaRepository<GridEntity, Long> {

	Optional<GridEntity> findByGridIdAndStore_StoreId(Long gridId, Long storeId);

	List<GridEntity> findByStore_StoreIdOrderByGridIdAsc(Long storeId);
}
