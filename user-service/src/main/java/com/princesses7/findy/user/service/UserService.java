package com.princesses7.findy.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.princesses7.findy.user.dto.request.SignupRequestDTO;
import com.princesses7.findy.user.entity.Grade;
import com.princesses7.findy.user.entity.Role;
import com.princesses7.findy.user.entity.UserEntity;
import com.princesses7.findy.user.entity.UserGradeEntity;
import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.repository.UserGradeRepository;
import com.princesses7.findy.user.repository.UserRepository;

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
			throw new BaseException(ErrorCode.INVALID_REQUEST, "이미 존재하는 이메일입니다.");
		}

		// 2. 기본 등급(BRONZE) 엔티티 조회
		UserGradeEntity defaultGrade = userGradeRepository.findByGradeName(Grade.BRONZE)
			.orElseThrow(() -> new BaseException(
				ErrorCode.INTERNAL_SERVER_ERROR,
				"기본 등급 데이터를 찾을 수 없습니다."));

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
