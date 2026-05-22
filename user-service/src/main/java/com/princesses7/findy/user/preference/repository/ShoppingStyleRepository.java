package com.princesses7.findy.user.preference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.user.preference.entity.ShoppingStyle;

public interface ShoppingStyleRepository extends JpaRepository<ShoppingStyle, Long> {

	List<ShoppingStyle> findAllByActiveTrue();
}