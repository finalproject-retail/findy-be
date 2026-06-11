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
import jakarta.persistence.Index;
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
	},
	indexes = {
		@Index(name = "idx_social_accounts_user_id", columnList = "user_id")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SocialAccount extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "social_account_id")
	private Long socialAccountId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 30)
	private SocialProvider provider;

	@Column(name = "provider_user_id", nullable = false)
	private String providerUserId;

	@Column(name = "provider_email")
	private String providerEmail;

	@Builder.Default
	@Column(name = "is_connected", nullable = false)
	private boolean isConnected = true;

	public static SocialAccount create(
		UserEntity user,
		SocialProvider provider,
		String providerUserId,
		String providerEmail
	) {
		return SocialAccount.builder()
			.user(user)
			.provider(provider)
			.providerUserId(providerUserId)
			.providerEmail(providerEmail)
			.isConnected(true)
			.build();
	}

	public void reconnect(UserEntity user, String providerEmail) {
		this.user = user;
		this.providerEmail = providerEmail;
		this.isConnected = true;
	}

	public void disconnect() {
		this.isConnected = false;
	}
}