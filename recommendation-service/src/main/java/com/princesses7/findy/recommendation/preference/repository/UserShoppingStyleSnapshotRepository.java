package com.princesses7.findy.recommendation.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.princesses7.findy.recommendation.preference.entity.UserShoppingStyleSnapshot;

public interface UserShoppingStyleSnapshotRepository extends JpaRepository<UserShoppingStyleSnapshot, Long> {

	@Query(value = """
		SELECT user_shopping_style_id, user_id, shopping_style_id
		FROM user_service.user_shopping_styles
		WHERE user_id = :userId
		""", nativeQuery = true)
	List<UserShoppingStyleSnapshot> findByUserId(@Param("userId") Long userId);
}