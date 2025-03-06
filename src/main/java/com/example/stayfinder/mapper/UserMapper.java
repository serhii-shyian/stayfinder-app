package com.example.stayfinder.mapper;

import com.example.stayfinder.config.MapperConfig;
import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    UserResponseDto toDto(User user);

    User toEntity(UserRegisterRequestDto registrationDto);
}
