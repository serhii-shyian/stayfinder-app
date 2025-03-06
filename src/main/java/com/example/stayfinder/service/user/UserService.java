package com.example.stayfinder.service.user;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto registrationDto)
            throws RegistrationException;
}
