package com.retail.apigateway.auth;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class GatewayPublicPathMatcher {

	private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

	public boolean isPublic(String requestUri, String method) {
		if (HttpMethod.OPTIONS.matches(method)) {
			return true;
		}

		return PATH_MATCHER.match("/actuator/**", requestUri)
			|| PATH_MATCHER.match("/api/v1/auth/login", requestUri)
			|| PATH_MATCHER.match("/api/v1/users/signup", requestUri);
	}
}
