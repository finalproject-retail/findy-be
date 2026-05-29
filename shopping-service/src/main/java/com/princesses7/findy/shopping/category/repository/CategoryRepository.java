package com.princesses7.findy.shopping.category.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.princesses7.findy.shopping.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

	List<Category> findByActiveTrueOrderByCategoryIdAsc();

	Optional<Category> findByCategoryIdAndActiveTrue(Long categoryId);
}
