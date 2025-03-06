package com.example.stayfinder.controller;

import com.example.stayfinder.dto.user.UserLoginRequestDto;
import com.example.stayfinder.dto.user.UserLoginResponseDto;
import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.RegistrationException;
import com.example.stayfinder.security.AuthService;
import com.example.stayfinder.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication management", description = "Endpoint for authenticate users")
public class AuthController {
    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Authenticate user",
            description = "Authentication user according to the parameters")
    public UserLoginResponseDto login(@RequestBody UserLoginRequestDto loginDto) {
        return authService.authenticate(loginDto);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register user",
            description = "Registering a new user according to the parameters")
    public UserResponseDto register(
            @RequestBody @Valid UserRegisterRequestDto registrationDto)
            throws RegistrationException {
        return userService.register(registrationDto);
    }
}
