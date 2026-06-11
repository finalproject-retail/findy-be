package com.princesses7.findy.user.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.princesses7.findy.user.social.entity.SocialAccount;
import com.princesses7.findy.user.social.entity.SocialProvider;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

	Optional<SocialAccount> findByProviderAndProviderUserId(
		SocialProvider provider,
		String providerUserId
	);

	Optional<SocialAccount> findByProviderAndProviderUserIdAndIsConnectedTrue(
		SocialProvider provider,
		String providerUserId
	);

	List<SocialAccount> findAllByUser_UserIdAndIsConnectedTrue(Long userId);

	boolean existsByProviderAndProviderUserIdAndIsConnectedTrue(
		SocialProvider provider,
		String providerUserId
	);
}