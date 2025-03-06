package com.example.stayfinder.security;

import com.example.stayfinder.dto.user.UserLoginRequestDto;
import com.example.stayfinder.dto.user.UserLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestLogin) {
        final Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestLogin.username(),
                        requestLogin.password()));

        String token = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(token);
    }
}
