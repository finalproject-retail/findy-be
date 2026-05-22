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
@Table(name = "user_shopping_styles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserShoppingStyle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_shopping_style_id")
	private Long userShoppingStyleId;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "shopping_style_id", nullable = false)
	private Long shoppingStyleId;

	private UserShoppingStyle(Long userId, Long shoppingStyleId) {
		this.userId = userId;
		this.shoppingStyleId = shoppingStyleId;
	}

	public static UserShoppingStyle create(Long userId, Long shoppingStyleId) {
		return new UserShoppingStyle(userId, shoppingStyleId);
	}
}