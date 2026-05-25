package com.princesses7.findy.recommendation.product.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.product.entity.ProductSnapshot;

public interface ProductSnapshotRepository extends JpaRepository<ProductSnapshot, Long> {

	List<ProductSnapshot> findByDeletedFalse(Pageable pageable);

	List<ProductSnapshot> findByProductIdIn(Collection<Long> productIds);

	List<ProductSnapshot> findByCategoryIdAndDeletedFalse(Long categoryId);
}