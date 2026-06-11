package com.princesses7.findy.user.social.entity;

import com.princesses7.findy.user.global.entity.BaseTimeEntity;
import com.princesses7.findy.user.user.entity.UserEntity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
	name = "social_accounts",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_social_accounts_provider_user",
			columnNames = {"provider", "provider_user_id"}
		)
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class SocialAccount extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long socialAccountId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(nullable = false, length = 30)
	@Enumerated(EnumType.STRING)
	private SocialProvider provider;

	@Column(nullable = false)
	private String providerUserId;

	private String providerEmail;

	@Builder.Default
	@Column(name = "is_connected", nullable = false)
	private boolean connected = true;

	public void disconnect() {
		this.connected = false;
	}
}
