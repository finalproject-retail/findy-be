package com.princesses7.findy.recommendation.preference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_preferred_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredCategorySnapshot {

	@Id
	@Column(name = "user_preferred_category_id")
	private Long userPreferredCategoryId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;
}