package com.retail.apigateway.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

class GatewayPublicPathMatcherTest {

	private final GatewayPublicPathMatcher matcher = new GatewayPublicPathMatcher();

	@Test
	void loginAndSignupArePublic() {
		assertThat(matcher.isPublic("/api/v1/auth/login", HttpMethod.POST.name())).isTrue();
		assertThat(matcher.isPublic("/api/v1/users/signup", HttpMethod.POST.name())).isTrue();
	}

	@Test
	void protectedApiRequiresAuth() {
		assertThat(matcher.isPublic("/api/v1/users/me", HttpMethod.GET.name())).isFalse();
		assertThat(matcher.isPublic("/api/v1/carts", HttpMethod.GET.name())).isFalse();
	}
}
