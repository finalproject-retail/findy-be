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
public class GoogleOAuthClient implements SocialOAuthClient {

	private final RestClient restClient;
	private final String clientId;
	private final String clientSecret;
	private final String defaultRedirectUri;
	private final String tokenUri;
	private final String userInfoUri;

	public GoogleOAuthClient(
		RestClient.Builder restClientBuilder,
		@Value("${oauth.google.client-id}") String clientId,
		@Value("${oauth.google.client-secret}") String clientSecret,
		@Value("${oauth.google.redirect-uri}") String defaultRedirectUri,
		@Value("${oauth.google.token-uri}") String tokenUri,
		@Value("${oauth.google.user-info-uri}") String userInfoUri
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
		return SocialProvider.GOOGLE == provider;
	}

	@Override
	public SocialUserProfile fetchProfile(String authorizationCode, String redirectUri) {
		assertConfigured(clientId, "구글 Client ID가 설정되지 않았습니다.");
		assertConfigured(clientSecret, "구글 Client Secret이 설정되지 않았습니다.");

		GoogleTokenResponse tokenResponse = requestToken(authorizationCode, resolveRedirectUri(redirectUri));
		GoogleUserInfoResponse userInfo = requestUserInfo(tokenResponse.accessToken());

		if (isBlank(userInfo.sub())) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "구글 사용자 식별자를 확인할 수 없습니다.");
		}

		return new SocialUserProfile(
			SocialProvider.GOOGLE,
			userInfo.sub(),
			userInfo.email(),
			isBlank(userInfo.name()) ? userInfo.email() : userInfo.name()
		);
	}

	private GoogleTokenResponse requestToken(String authorizationCode, String redirectUri) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("code", authorizationCode);
		body.add("client_id", clientId);
		body.add("client_secret", clientSecret);
		body.add("redirect_uri", redirectUri);

		try {
			GoogleTokenResponse response = restClient.post()
				.uri(tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(body)
				.retrieve()
				.body(GoogleTokenResponse.class);

			if (response == null || isBlank(response.accessToken())) {
				throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "구글 액세스 토큰을 발급받지 못했습니다.");
			}
			return response;
		} catch (RestClientException exception) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "구글 토큰 요청에 실패했습니다.");
		}
	}

	private GoogleUserInfoResponse requestUserInfo(String accessToken) {
		try {
			GoogleUserInfoResponse response = restClient.get()
				.uri(userInfoUri)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.body(GoogleUserInfoResponse.class);

			return Objects.requireNonNullElseGet(response, GoogleUserInfoResponse::empty);
		} catch (RestClientException exception) {
			throw new BaseException(ErrorCode.SOCIAL_AUTH_FAILED, "구글 사용자 정보 조회에 실패했습니다.");
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

	private record GoogleTokenResponse(
		@JsonProperty("access_token") String accessToken,
		@JsonProperty("token_type") String tokenType,
		@JsonProperty("expires_in") Long expiresIn,
		@JsonProperty("scope") String scope,
		@JsonProperty("id_token") String idToken
	) {
	}

	private record GoogleUserInfoResponse(
		String sub,
		String email,
		@JsonProperty("email_verified") Boolean emailVerified,
		String name,
		String picture
	) {
		private static GoogleUserInfoResponse empty() {
			return new GoogleUserInfoResponse(null, null, null, null, null);
		}
	}
}
