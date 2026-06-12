package com.retail.apigateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8081,https://d3fdsv269tc0ul.cloudfront.net}")
	private String allowedOrigins;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers("/api/v1/auth/login").permitAll()
				.requestMatchers("/api/v1/auth/social/**").permitAll()
				.requestMatchers("/api/v1/auth/password-reset/**").permitAll()
				.requestMatchers("/api/v1/email-verifications/**").permitAll()
				.requestMatchers("/api/v1/users/signup").permitAll()
				.requestMatchers("/actuator/health").permitAll()
				.anyRequest().permitAll()
			);

		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.setAllowedOrigins(parseAllowedOrigins());

		configuration.setAllowedMethods(List.of(
			"GET",
			"POST",
			"PUT",
			"PATCH",
			"DELETE",
			"OPTIONS"
		));

		configuration.setAllowedHeaders(List.of("*"));

		configuration.setExposedHeaders(List.of(
			"Authorization",
			"X-USER-ID",
			"X-User-Id"
		));

		configuration.setAllowCredentials(true);
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	private List<String> parseAllowedOrigins() {
		return Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(origin -> !origin.isEmpty())
			.toList();
	}
}
