package com.princesses7.findy.user.user.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.princesses7.findy.user.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class UserEntity extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(unique = true)
	private String email;

	private String password; // 소셜 로그인 고려하여 null 허용 가능

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private String phoneNumber;

	@Column(nullable = false)
	private LocalDate birthDate;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Gender gender; //Enum (MALE, FEMALE)

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Role role; // Enum (ROLE_USER, ROLE_ADMIN)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade_id", nullable = false)
	private UserGradeEntity grade; // Enum (BRONZE, SILVER, GOLD, VIP)

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Builder.Default
	@Column(nullable = false)
	private double reward = 0;

	@Builder.Default
	@Column(nullable = false)
	private long purchaseAmount = 0;

	@Builder.Default
	@Column(nullable = false)
	private boolean isFirstLogin = true;
}
