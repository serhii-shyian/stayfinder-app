package com.example.stayfinder.mapper;

import com.example.stayfinder.config.MapperConfig;
import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.model.Role;
import com.example.stayfinder.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringSet")
    UserResponseDto toDto(User user);

    User toEntity(UserRegisterRequestDto registrationDto);

    void updateEntityFromDto(@MappingTarget User user, UserRegisterRequestDto updateDto);

    @Named("rolesToStringSet")
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
