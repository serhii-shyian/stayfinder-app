package com.example.stayfinder.dto.user;

import com.example.stayfinder.validation.Password;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank(message = "Username may not be blank")
        String username,
        @NotBlank(message = "Password may not be blank")
        @Password
        String password) {
}
