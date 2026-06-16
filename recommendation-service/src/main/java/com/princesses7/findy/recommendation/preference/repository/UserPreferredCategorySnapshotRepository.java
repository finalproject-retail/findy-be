package com.princesses7.findy.recommendation.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.preference.entity.UserPreferredCategorySnapshot;

public interface UserPreferredCategorySnapshotRepository extends JpaRepository<UserPreferredCategorySnapshot, Long> {

	@Query(value = """
		SELECT user_preferred_category_id, user_id, category_id
		FROM user_service.user_preferred_categories
		WHERE user_id = :userId
		""", nativeQuery = true)
	List<UserPreferredCategorySnapshot> findByUserId(@Param("userId") Long userId);
}