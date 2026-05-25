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
@Table(name = "user_shopping_styles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserShoppingStyleSnapshot {

	@Id
	@Column(name = "user_shopping_style_id")
	private Long userShoppingStyleId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "shopping_style_id", nullable = false)
	private Long shoppingStyleId;
}