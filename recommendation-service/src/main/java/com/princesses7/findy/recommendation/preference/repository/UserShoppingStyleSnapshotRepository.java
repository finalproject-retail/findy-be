package com.princesses7.findy.recommendation.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.preference.entity.UserShoppingStyleSnapshot;

public interface UserShoppingStyleSnapshotRepository extends JpaRepository<UserShoppingStyleSnapshot, Long> {

	List<UserShoppingStyleSnapshot> findByUserId(Long userId);
}