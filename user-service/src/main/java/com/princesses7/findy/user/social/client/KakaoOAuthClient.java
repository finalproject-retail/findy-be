package com.princesses7.findy.user.social.client;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.social.dto.response.SocialUserProfile;
import com.princesses7.findy.user.social.entity.SocialProvider;

@Component
public class KakaoOAuthClient implements SocialOAuthClient {

	private final RestClient restClient;
	private final String clientId;
	private final String clientSecret;
	private final String defaultRedirectUri;
	private final String tokenUri;
	private final String userInfoUri;

	public KakaoOAuthClient(
		RestClient.Builder restClientBuilder,
		@Value("${oauth.kakao.client-id}") String clientId,
		@Value("${oauth.kakao.client-secret}") String clientSecret,
		@Value("${oauth.kakao.redirect-uri}") String defaultRedirectUri,
		@Value("${oauth.kakao.token-uri}") String tokenUri,
		@Value("${oauth.kakao.user-info-uri}") String userInfoUri
	) {
		this.restClient = restClientBuilder.build();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.defaultRedirectUri = defaultRedirectUri;
		this.tokenUri = tokenUri;
		this.userInfoUri = userInfoUri;
	}

	@Override
	public boolean supports(SocialProvider provider) {
		return SocialProvider.KAKAO == provider;
	}

	@Override
	public SocialUserProfile fetchProfile(String authorizationCode, String redirectUri) {
		assertConfigured(clientId, "카카오 REST API 키가 설정되지 않았습니다.");

		KakaoTokenResponse tokenResponse = requestToken(authorizationCode, resolveRedirectUri(redirectUri));
		KakaoUserInfoResponse userInfo = requestUserInfo(tokenResponse.accessToken());

		if (userInfo.id() == null) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "카카오 사용자 식별자를 확인할 수 없습니다.");
		}

		String email = userInfo.kakaoAccount() == null ? null : userInfo.kakaoAccount().email();
		String nickname = null;
		if (userInfo.kakaoAccount() != null && userInfo.kakaoAccount().profile() != null) {
			nickname = userInfo.kakaoAccount().profile().nickname();
		}

		return new SocialUserProfile(
			SocialProvider.KAKAO,
			String.valueOf(userInfo.id()),
			email,
			isBlank(nickname) ? email : nickname
		);
	}

	private KakaoTokenResponse requestToken(String authorizationCode, String redirectUri) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("code", authorizationCode);
		body.add("client_id", clientId);
		body.add("redirect_uri", redirectUri);
		if (!isBlank(clientSecret)) {
			body.add("client_secret", clientSecret);
		}

		try {
			KakaoTokenResponse response = restClient.post()
				.uri(tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(body)
				.retrieve()
				.body(KakaoTokenResponse.class);

			if (response == null || isBlank(response.accessToken())) {
				throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "카카오 액세스 토큰을 발급받지 못했습니다.");
			}
			return response;
		} catch (RestClientException exception) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "카카오 토큰 요청에 실패했습니다.");
		}
	}

	private KakaoUserInfoResponse requestUserInfo(String accessToken) {
		try {
			KakaoUserInfoResponse response = restClient.get()
				.uri(userInfoUri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.body(KakaoUserInfoResponse.class);

			return Objects.requireNonNullElseGet(response, KakaoUserInfoResponse::empty);
		} catch (RestClientException exception) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "카카오 사용자 정보 조회에 실패했습니다.");
		}
	}

	private String resolveRedirectUri(String redirectUri) {
		return isBlank(redirectUri) ? defaultRedirectUri : redirectUri;
	}

	private void assertConfigured(String value, String message) {
		if (isBlank(value)) {
			throw new BaseException(ErrorCode.SOCIAL_CLIENT_NOT_CONFIGURED, message);
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private record KakaoTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("refresh_token") String refreshToken,
		@JsonProperty("expires_in") Long expiresIn,
		@JsonProperty("refresh_token_expires_in") Long refreshTokenExpiresIn,
		String scope
	) {
	}

	private record KakaoUserInfoResponse(
		Long id,
		@JsonProperty("kakao_account") KakaoAccount kakaoAccount
	) {
		private static KakaoUserInfoResponse empty() {
			return new KakaoUserInfoResponse(null, null);
		}
	}

	private record KakaoAccount(
		String email,
		KakaoProfile profile
	) {
	}

	private record KakaoProfile(
		String nickname
	) {
	}
}
