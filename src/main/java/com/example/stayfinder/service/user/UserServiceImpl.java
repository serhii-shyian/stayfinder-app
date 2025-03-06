package com.example.stayfinder.service.user;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.RegistrationException;
import com.example.stayfinder.mapper.UserMapper;
import com.example.stayfinder.model.Role;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.role.RoleRepository;
import com.example.stayfinder.repository.user.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(UserRegisterRequestDto registrationDto)
            throws RegistrationException {
        if (userRepository.findByUsername(registrationDto.username()).isPresent()) {
            throw new RegistrationException(
                    String.format("User with username %s already exists",
                            registrationDto.username()));
        }

        User userFromDto = userMapper.toEntity(registrationDto);
        userFromDto.setPassword(passwordEncoder.encode(registrationDto.password()));
        userFromDto.setRoles(findByNameContaining(Set.of(
                Role.RoleName.USER)));

        User savedUser = userRepository.save(userFromDto);

        return userMapper.toDto(savedUser);
    }

    private Set<Role> findByNameContaining(Set<Role.RoleName> rolesSet) {
        return new HashSet<>(roleRepository.findAllByNameContaining(rolesSet));
    }
}
