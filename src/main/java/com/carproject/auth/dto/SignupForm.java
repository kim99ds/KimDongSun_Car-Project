package com.carproject.auth.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter @Setter
public class SignupForm {

    @NotBlank
    @Size(min = 4, max = 20)
    private String loginId;

    @NotBlank
    @Size(min = 4, max = 30)
    private String password;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}

