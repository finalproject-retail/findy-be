package com.princesses7.findy.recommendation.preference.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.preference.entity.CategorySnapshot;

public interface CategorySnapshotRepository extends JpaRepository<CategorySnapshot, Long> {

	@Query(value = """
		SELECT category_id, parent_category_id, category_name, is_active
		FROM shopping_service.categories
		WHERE category_id IN (:categoryIds)
		""", nativeQuery = true)
	List<CategorySnapshot> findByCategoryIdIn(@Param("categoryIds") Collection<Long> categoryIds);

	List<CategorySnapshot> findByCategoryNameContainingIgnoreCaseAndActiveTrue(String categoryName);
}