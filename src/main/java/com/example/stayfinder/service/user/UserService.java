package com.example.stayfinder.service.user;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.RegistrationException;
import com.example.stayfinder.model.User;
import java.util.List;

public interface UserService {
    UserResponseDto register(UserRegisterRequestDto registrationDto)
            throws RegistrationException;

    UserResponseDto updateUserRoles(Long userId, List<String> roleNames);

    UserResponseDto findProfile(User user);

    UserResponseDto updateProfile(User user, UserRegisterRequestDto updateDto);
}
