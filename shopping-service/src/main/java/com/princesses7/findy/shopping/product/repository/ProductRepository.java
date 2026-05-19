package com.princesses7.findy.shopping.product.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.princesses7.findy.shopping.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Page<Product> findByIsDeletedFalse(Pageable pageable);

	Page<Product> findByCategoryIdAndIsDeletedFalse(Long categoryId, Pageable pageable);

	Optional<Product> findByProductIdAndIsDeletedFalse(Long productId);

	boolean existsByBarcodeAndIsDeletedFalse(String barcode);

	Optional<Product> findByBarcodeAndIsDeletedFalse(String barcode);
}
