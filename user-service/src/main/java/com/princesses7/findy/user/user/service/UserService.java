package com.princesses7.findy.user.user.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.princesses7.findy.user.global.exception.BaseException;
import com.princesses7.findy.user.global.exception.ErrorCode;
import com.princesses7.findy.user.user.dto.request.SignupRequestDTO;
import com.princesses7.findy.user.user.dto.response.MyPageResponse;
import com.princesses7.findy.user.user.entity.Grade;
import com.princesses7.findy.user.user.entity.Role;
import com.princesses7.findy.user.user.entity.UserEntity;
import com.princesses7.findy.user.user.entity.UserGradeEntity;
import com.princesses7.findy.user.user.repository.UserGradeRepository;
import com.princesses7.findy.user.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final UserGradeRepository userGradeRepository;
	private final BCryptPasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public MyPageResponse getMyPage(Long userId) {
		UserEntity user = userRepository.findById(userId)
			.orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

		if (user.getDeletedAt() != null) {
			throw new BaseException(ErrorCode.DELETED_USER);
		}

		return MyPageResponse.from(user);
	}

	@Transactional
	public Long signup(SignupRequestDTO request) {
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

		UserEntity savedUser = userRepository.save(user);

		return savedUser.getUserId();
	}
}
