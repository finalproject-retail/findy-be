package com.princesses7.findy.user.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.user.preference.entity.UserPreferredCategory;

public interface UserPreferredCategoryRepository extends JpaRepository<UserPreferredCategory, Long> {

	List<UserPreferredCategory> findAllByUserId(Long userId);

	void deleteAllByUserId(Long userId);
}