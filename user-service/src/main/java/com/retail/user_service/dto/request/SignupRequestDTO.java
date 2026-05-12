package com.retail.user_service.dto.request;

import java.time.LocalDate;

import com.retail.user_service.entity.Gender;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private LocalDate birthDate;

    @NotNull
    private Gender gender;
}