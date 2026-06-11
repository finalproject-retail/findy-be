package com.princesses7.findy.user.social.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.user.social.entity.SocialAccount;
import com.princesses7.findy.user.social.entity.SocialProvider;
import com.princesses7.findy.user.user.entity.UserEntity;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findByProviderAndProviderUserIdAndConnectedTrue(
		SocialProvider provider,
		String providerUserId
	);

	boolean existsByUserAndProviderAndConnectedTrue(UserEntity user, SocialProvider provider);
}