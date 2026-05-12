package com.retail.user_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.retail.user_service.dto.request.LoginRequestDTO;
import com.retail.user_service.dto.response.TokenResponseDTO;
import com.retail.user_service.entity.UserEntity;
import com.retail.user_service.repository.UserRepository;
import com.retail.user_service.security.JwtProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtProvider.createToken(
                String.valueOf(user.getUserId()),
                user.getEmail(),
                user.getRole().name()
        );

        return new TokenResponseDTO(token);
    }
}