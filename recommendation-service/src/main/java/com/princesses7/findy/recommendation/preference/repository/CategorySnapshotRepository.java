package com.princesses7.findy.recommendation.preference.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;

public interface CategorySnapshotRepository extends JpaRepository<CategorySnapshot, Long> {

	List<CategorySnapshot> findByCategoryIdIn(Collection<Long> categoryIds);

	List<CategorySnapshot> findByCategoryNameContainingIgnoreCaseAndActiveTrue(String categoryName);
}