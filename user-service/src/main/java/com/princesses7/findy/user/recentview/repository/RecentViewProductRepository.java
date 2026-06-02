package com.princesses7.findy.user.recentview.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.user.recentview.entity.RecentViewProduct;

public interface RecentViewProductRepository extends JpaRepository<RecentViewProduct, Long> {

	Optional<RecentViewProduct> findByUserIdAndProductId(Long userId, Long productId);

	List<RecentViewProduct> findAllByUserIdOrderByViewedAtDescRecentViewIdDesc(
		Long userId,
		Pageable pageable
	);
}