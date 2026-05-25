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
@Table(name = "shopping_styles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShoppingStyle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "shopping_style_id")
	private Long shoppingStyleId;

	@Column(name = "style_name", nullable = false)
	private String styleName;

	@Column(name = "is_active", nullable = false)
	private boolean active;
}