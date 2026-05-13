package com.princesses7.findy.product.domain.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.princesses7.findy.product.domain.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Page<Product> findByIsDeletedFalse(Pageable pageable);

	Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

	Optional<Product> findByProductIdAndIsDeletedFalse(Long productId);
}
