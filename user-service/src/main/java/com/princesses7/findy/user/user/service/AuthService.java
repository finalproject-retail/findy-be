package com.princesses7.findy.user.user.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.security.JwtProvider;
import com.princesses7.findy.user.user.dto.request.LoginRequestDTO;
import com.princesses7.findy.user.user.dto.response.TokenResponseDTO;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final JwtProvider jwtProvider;
	private final BCryptPasswordEncoder passwordEncoder;

	public TokenResponseDTO login(LoginRequestDTO request) {
		UserEntity user = userRepository.findByEmail(request.getEmail())
			.orElseThrow(() -> new BaseException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."));

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new BaseException(ErrorCode.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.");
		}

		String token = jwtProvider.createToken(
			String.valueOf(user.getUserId()),
			user.getEmail(),
			user.getRole().name()
		);

		return new TokenResponseDTO(token);
	}

	public void logout() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null
			|| !authentication.isAuthenticated()
			|| authentication instanceof AnonymousAuthenticationToken) {
			throw new BaseException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
		}
		Object credentials = authentication.getCredentials();
		if (!(credentials instanceof String rawToken) || rawToken.isBlank()) {
			throw new BaseException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
		}
		jwtProvider.invalidateToken(rawToken);
	}
}