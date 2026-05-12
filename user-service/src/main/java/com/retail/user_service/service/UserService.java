package com.retail.user_service.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.retail.user_service.dto.request.SignupRequestDTO;
import com.retail.user_service.entity.Grade;
import com.retail.user_service.entity.Role;
import com.retail.user_service.entity.UserEntity;
import com.retail.user_service.entity.UserGradeEntity;
import com.retail.user_service.repository.UserGradeRepository;
import com.retail.user_service.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserGradeRepository userGradeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequestDTO request) {
        // 1. 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 2. 기본 등급(BRONZE) 엔티티 조회
        UserGradeEntity defaultGrade = userGradeRepository.findByGradeName(Grade.BRONZE)
                .orElseThrow(() -> new RuntimeException("기본 등급 데이터를 찾을 수 없습니다."));

        // 3. 회원 저장
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .role(Role.ROLE_USER)
                .grade(defaultGrade)
                .reward(0)
                .purchaseAmount(0)
                .isFirstLogin(true)
                .build();

        userRepository.save(user);
    }
}
