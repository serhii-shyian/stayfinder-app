package com.example.stayfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.RegistrationException;
import com.example.stayfinder.mapper.UserMapper;
import com.example.stayfinder.model.Role;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.role.RoleRepository;
import com.example.stayfinder.repository.user.UserRepository;
import com.example.stayfinder.service.user.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("""
            Register a new user when the username is not taken
            """)
    public void register_ValidUserRegisterRequestDto_ReturnsUserResponseDto()
            throws RegistrationException {
        //Given
        UserRegisterRequestDto requestDto = getUserRegisterRequestDto();
        User userFromDto = getUserFromDto(requestDto);
        User savedUser = getUser();
        UserResponseDto expected = getUserResponseDto();

        when(userRepository.findByUsername(requestDto.username()))
                .thenReturn(Optional.empty());
        when(roleRepository.findAllByNameContaining(Set.of(Role.RoleName.USER)))
                .thenReturn(Set.of(getUserRole()));
        when(userMapper.toEntity(requestDto)).thenReturn(userFromDto);
        when(passwordEncoder.encode(requestDto.password()))
                .thenReturn("encodedPassword");
        when(userRepository.save(userFromDto)).thenReturn(savedUser);
        when(userMapper.toDto(savedUser)).thenReturn(expected);

        //When
        UserResponseDto actual = userService.register(requestDto);

        //Then
        assertEquals(expected, actual);
        verifyNoMoreInteractions(userRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("""
            Register a user when the username is already taken
            """)
    public void register_UsernameAlreadyExists_ThrowsException() {
        //Given
        UserRegisterRequestDto requestDto = getUserRegisterRequestDto();

        when(userRepository.findByUsername(requestDto.username()))
                .thenReturn(Optional.of(getUser()));

        //Then
        assertThrows(RegistrationException.class,
                () -> userService.register(requestDto));
        verifyNoMoreInteractions(userRepository);
    }

    private UserRegisterRequestDto getUserRegisterRequestDto() {
        return new UserRegisterRequestDto(
                "john.doe",
                "qwerty123",
                "qwerty123",
                "john.doe@example.com",
                "John",
                "Doe");
    }

    private User getUser() {
        return new User()
                .setId(1L)
                .setUsername("username")
                .setPassword("encodedPassword")
                .setEmail("email@example.com")
                .setRoles(Set.of(getUserRole()));
    }

    private Role getUserRole() {
        return new Role()
                .setId(1L)
                .setName(Role.RoleName.USER);
    }

    private User getUserFromDto(UserRegisterRequestDto requestDto) {
        return new User()
                .setUsername(requestDto.username())
                .setPassword(requestDto.password())
                .setEmail(requestDto.email());
    }

    private UserResponseDto getUserResponseDto() {
        return new UserResponseDto(
                1L,
                "john.doe",
                "john.doe@example.com",
                "John",
                "Doe",
                Set.of("USER"));
    }
}
