package com.princesses7.findy.recommendation.preference.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.preference.entity.ShoppingStyleSnapshot;

public interface ShoppingStyleSnapshotRepository extends JpaRepository<ShoppingStyleSnapshot, Long> {

	@Query(value = """
		SELECT shopping_style_id, style_name, is_active
		FROM user_service.shopping_styles
		WHERE shopping_style_id IN (:shoppingStyleIds)
		""", nativeQuery = true)
	List<ShoppingStyleSnapshot> findByShoppingStyleIdIn(@Param("shoppingStyleIds") Collection<Long> shoppingStyleIds);
}