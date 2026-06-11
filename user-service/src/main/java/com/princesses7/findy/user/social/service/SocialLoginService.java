package com.princesses7.findy.user.social.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.security.JwtProvider;
import com.princesses7.findy.user.social.client.SocialOAuthClient;
import com.princesses7.findy.user.social.dto.request.SocialLoginRequest;
import com.princesses7.findy.user.social.dto.response.SocialLoginResponse;
import com.princesses7.findy.user.social.dto.response.SocialUserProfile;
import com.princesses7.findy.user.social.entity.SocialAccount;
import com.princesses7.findy.user.social.entity.SocialProvider;
import com.princesses7.findy.user.social.repository.SocialAccountRepository;
import com.princesses7.findy.user.user.entity.Gender;
import com.princesses7.findy.user.user.entity.Grade;
import com.princesses7.findy.user.user.entity.Role;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.entity.UserGradeEntity;
import com.princesses7.findy.user.user.repository.UserGradeRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialLoginService {

	private final List<SocialOAuthClient> socialOAuthClients;
	private final SocialAccountRepository socialAccountRepository;
	private final UserRepository userRepository;
	private final UserGradeRepository userGradeRepository;
	private final JwtProvider jwtProvider;

	@Transactional
	public SocialLoginResponse login(SocialProvider provider, SocialLoginRequest request) {
		SocialUserProfile profile = getClient(provider).fetchProfile(request.code(), request.redirectUri());

		SocialAccount socialAccount = socialAccountRepository
			.findByProviderAndProviderUserIdAndConnectedTrue(provider, profile.providerUserId())
			.orElseGet(() -> connectOrCreateSocialAccount(profile));

		UserEntity user = socialAccount.getUser();
		String token = jwtProvider.createToken(
			String.valueOf(user.getUserId()),
			user.getEmail(),
			user.getRole().name()
		);

		return new SocialLoginResponse(
			user.getUserId(),
			user.getEmail(),
			user.getName(),
			provider,
			user.isFirstLogin(),
			token
		);
	}

	private SocialOAuthClient getClient(SocialProvider provider) {
		return socialOAuthClients.stream()
			.filter(client -> client.supports(provider))
			.findFirst()
			.orElseThrow(() -> new BaseException(ErrorCode.UNSUPPORTED_SOCIAL_PROVIDER));
	}

	private SocialAccount connectOrCreateSocialAccount(SocialUserProfile profile) {
		UserEntity user = resolveUser(profile);

		if (socialAccountRepository.existsByUserAndProviderAndConnectedTrue(user, profile.provider())) {
			throw new BaseException(ErrorCode.SOCIAL_ACCOUNT_ALREADY_CONNECTED);
		}

		return socialAccountRepository.save(
			SocialAccount.builder()
				.user(user)
				.provider(profile.provider())
				.providerUserId(profile.providerUserId())
				.providerEmail(profile.email())
				.connected(true)
				.build()
		);
	}

	private UserEntity resolveUser(SocialUserProfile profile) {
		String email = resolveEmail(profile);

		return userRepository.findByEmail(email)
			.orElseGet(() -> createSocialUser(profile, email));
	}

	private UserEntity createSocialUser(SocialUserProfile profile, String email) {
		UserGradeEntity defaultGrade = userGradeRepository.findByGradeName(Grade.BRONZE)
			.orElseThrow(() -> new BaseException(
				ErrorCode.INTERNAL_SERVER_ERROR,
				"기본 등급 데이터를 찾을 수 없습니다."
			));

		String name = isBlank(profile.name()) ? profile.provider().name() + " 사용자" : profile.name();

		return userRepository.save(
			UserEntity.builder()
				.email(email)
				.password(null)
				.name(name)
				.phoneNumber("SOCIAL-" + profile.provider().name() + "-" + profile.providerUserId())
				.birthDate(LocalDate.of(1970, 1, 1))
				.gender(Gender.UNKNOWN)
				.role(Role.ROLE_USER)
				.grade(defaultGrade)
				.reward(0)
				.purchaseAmount(0)
				.isFirstLogin(true)
				.build()
		);
	}

	private String resolveEmail(SocialUserProfile profile) {
		if (!isBlank(profile.email())) {
			return profile.email();
		}
		return profile.provider().name().toLowerCase() + "_" + profile.providerUserId() + "@social.findy.local";
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}
