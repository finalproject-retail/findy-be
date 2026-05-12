package com.retail.user_service.dto.response;

import java.time.LocalDate;

import com.retail.user_service.entity.Gender;
import com.retail.user_service.entity.Role;
import com.retail.user_service.entity.UserGradeEntity;

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
