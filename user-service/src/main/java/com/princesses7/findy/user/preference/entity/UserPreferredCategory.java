package com.princesses7.findy.user.preference.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_preferred_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferredCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_preferred_category_id")
	private Long userPreferredCategoryId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "category_id", nullable = false)
	private Long categoryId;

	private UserPreferredCategory(Long userId, Long categoryId) {
		this.userId = userId;
		this.categoryId = categoryId;
	}

	public static UserPreferredCategory create(Long userId, Long categoryId) {
		return new UserPreferredCategory(userId, categoryId);
	}
}