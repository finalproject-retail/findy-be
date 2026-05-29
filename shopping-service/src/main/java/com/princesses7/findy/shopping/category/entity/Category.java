package com.princesses7.findy.shopping.category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.princesses7.findy.shopping.global.entity.BaseTimeEntity;

@Getter
@Entity
@Table(name = "categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

	@Id
	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "parent_category_id")
	private Long parentCategoryId;

	@Column(name = "category_name", nullable = false)
	private String categoryName;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "grid_id")
	private Long gridId;
}
