package com.example.stayfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.exception.RegistrationException;
import com.example.stayfinder.mapper.UserMapper;
import com.example.stayfinder.model.Role;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.role.RoleRepository;
import com.example.stayfinder.repository.user.UserRepository;
import com.example.stayfinder.service.user.UserServiceImpl;
import java.util.List;
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
        verify(userRepository).findByUsername(requestDto.username());
        verify(roleRepository).findAllByNameContaining(Set.of(Role.RoleName.USER));
        verify(userMapper).toEntity(requestDto);
        verify(passwordEncoder).encode(requestDto.password());
        verify(userRepository).save(userFromDto);
        verify(userMapper).toDto(savedUser);
        verifyNoMoreInteractions(
                userRepository, roleRepository, userMapper, passwordEncoder);
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
        verify(userRepository).findByUsername(requestDto.username());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("""
            Update user roles when user exists
            """)
    public void updateUserRoles_ExistingUserId_ReturnsUpdatedUser() {
        //Given
        User user = getUser();
        List<String> roleNames = List.of("ADMIN");
        Set<Role.RoleName> roleNamesSet = Set.of(Role.RoleName.ADMIN);
        Set<Role> roles = Set.of(getAdminRole());
        UserResponseDto expected = getUserResponseDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(roleRepository.findAllByNameContaining(roleNamesSet)).thenReturn(roles);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        //When
        UserResponseDto actual = userService.updateUserRoles(user.getId(), roleNames);

        //Then
        assertEquals(expected, actual);
        verify(userRepository).findById(user.getId());
        verify(roleRepository).findAllByNameContaining(roleNamesSet);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper);
    }

    @Test
    @DisplayName("""
            Update user roles when user does not exist
            """)
    public void updateUserRoles_NonExistingUserId_ThrowsException() {
        //Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        //Then
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRoles(99L, List.of("ADMIN")));
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("""
            Find user profile successfully when user exists
            """)
    public void findProfile_ExistingUser_ReturnsUserProfile() {
        // Given
        User user = getUser();
        UserResponseDto expected = getUserResponseDto();

        when(userMapper.toDto(user)).thenReturn(expected);

        // When
        UserResponseDto actual = userService.findProfile(user);

        // Then
        assertEquals(expected, actual);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    @DisplayName("""
            Update user profile successfully when user exists and data is valid
            """)
    public void updateProfile_ExistingUser_UpdatesProfile() {
        // Given
        User user = getUser();
        UserRegisterRequestDto updateDto = getUserRegisterRequestDto();
        UserResponseDto expected = getUserResponseDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntityFromDto(user, updateDto);
        when(passwordEncoder.encode(updateDto.password()))
                .thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expected);

        // When
        UserResponseDto actual = userService.updateProfile(user, updateDto);

        // Then
        assertEquals(expected, actual);
        verify(userRepository).findById(user.getId());
        verify(userMapper).updateEntityFromDto(user, updateDto);
        verify(passwordEncoder).encode(updateDto.password());
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userMapper, userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("""
            Throw exception when updating profile of a non-existent user
            """)
    public void updateProfile_NonExistentUser_ThrowsException() {
        // Given
        User user = getUser();
        UserRegisterRequestDto updateDto = getUserRegisterRequestDto();

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateProfile(user, updateDto));
        verify(userRepository).findById(user.getId());
        verifyNoMoreInteractions(userMapper, passwordEncoder, userRepository);
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

    private Role getAdminRole() {
        return new Role()
                .setId(2L)
                .setName(Role.RoleName.ADMIN);
    }
}
