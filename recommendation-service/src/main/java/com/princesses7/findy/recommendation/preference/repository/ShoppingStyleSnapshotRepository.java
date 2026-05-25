package com.princesses7.findy.recommendation.preference.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.recommendation.preference.entity.ShoppingStyleSnapshot;

public interface ShoppingStyleSnapshotRepository extends JpaRepository<ShoppingStyleSnapshot, Long> {

	List<ShoppingStyleSnapshot> findByShoppingStyleIdIn(Collection<Long> shoppingStyleIds);
}