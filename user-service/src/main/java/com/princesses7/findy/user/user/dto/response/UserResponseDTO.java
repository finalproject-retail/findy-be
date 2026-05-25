package com.princesses7.findy.user.user.dto.response;

import java.time.LocalDate;

import com.princesses7.findy.user.user.entity.Gender;
import com.princesses7.findy.user.user.entity.Role;
import com.princesses7.findy.user.user.entity.UserGradeEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
	private Long userId;
	private String email;
	private String name;
	private String phoneNumber;
	private LocalDate birthDate;
	private Gender gender;
	private Role role;
	private UserGradeEntity grade;
	private int reward;
	private long purchaseAmount;
	private boolean isFirstLogin;
}
