package com.princesses7.findy.user.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.user.preference.entity.UserShoppingStyle;

public interface UserShoppingStyleRepository extends JpaRepository<UserShoppingStyle, Long> {

	List<UserShoppingStyle> findAllByUserId(Long userId);

	void deleteAllByUserId(Long userId);
}