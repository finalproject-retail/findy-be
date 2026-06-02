package com.retail.apigateway.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

	private final GatewayPublicPathMatcher publicPathMatcher;
	private final GatewayJwtValidator jwtValidator;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String requestUri = request.getRequestURI();

		if (!requestUri.startsWith("/api/")) {
			filterChain.doFilter(request, response);
			return;
		}

		if (publicPathMatcher.isPublic(requestUri, request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		Long userId = jwtValidator.resolveUserId(request.getHeader(GatewayAuthHeaders.AUTHORIZATION));
		if (userId == null) {
			writeUnauthorized(response);
			return;
		}

		filterChain.doFilter(
			new UserIdHeaderRequestWrapper(request, String.valueOf(userId)),
			response
		);
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(
			"{\"success\":false,\"code\":\"AUTH_004\",\"message\":\"유효하지 않은 토큰입니다.\",\"data\":null}"
		);
	}
}
