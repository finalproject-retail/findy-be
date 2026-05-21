package com.princesses7.findy.recommendation.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.preference.entity.UserPreferredCategorySnapshot;

public interface UserPreferredCategorySnapshotRepository extends JpaRepository<UserPreferredCategorySnapshot, Long> {

	List<UserPreferredCategorySnapshot> findByUserId(Long userId);
}