package com.princesses7.findy.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.princesses7.findy.user.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.csrf(csrf -> csrf.disable()) // 로컬/MSA 환경에선 보통 끔
			.sessionManagement(
				session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 대신 JWT 사용
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/api/v1/auth/login",
					"/api/v1/auth/logout",
					"/api/v1/auth/social/**",
					"/api/v1/auth/password-reset/**",
					"/api/v1/auth/email/**",
					"/api/v1/users/signup",
					"/actuator/health",
					"/actuator/prometheus",
					"/internal/**"
				)
				.permitAll()
				.anyRequest().authenticated() // 나머지는 토큰 없으면 차단!
			)
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
