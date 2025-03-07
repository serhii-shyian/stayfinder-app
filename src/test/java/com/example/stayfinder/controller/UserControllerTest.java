package com.example.stayfinder.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.example.stayfinder.dto.user.UserRoleUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/roles/insert-into-roles.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/users/insert-into-users.sql"));
        }
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/users/delete-all-from-users.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/roles/delete-all-from-roles.sql"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("""
            Update user roles when admin is authenticated
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateUserRoles_AdminAuthenticated_ReturnsUpdatedUserResponseDto()
            throws Exception {
        // Given
        UserRoleUpdateRequestDto requestDto = getAdminRolesUpdateDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        UserResponseDto expected = getUpdatedAdminDto();

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/3/role")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserResponseDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Order(2)
    @DisplayName("""
            Get profile when user is authenticated
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void getProfile_UserAuthenticated_ReturnsUserProfile() throws Exception {
        // Given
        UserResponseDto expected = getUserResponseDto();

        // When
        MvcResult result = mockMvc.perform(
                        get("/users/me")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserResponseDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("""
            Update profile when user is authenticated
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void updateProfile_UserAuthenticated_ReturnsUpdatedUserProfile() throws Exception {
        // Given
        UserRegisterRequestDto updateDto = getUpdateUserRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(updateDto);
        UserResponseDto expected = getUpdatedUserDto();

        // When
        MvcResult result = mockMvc.perform(
                        put("/users/me")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), UserResponseDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    private UserRoleUpdateRequestDto getAdminRolesUpdateDto() {
        return new UserRoleUpdateRequestDto(List.of("USER", "ADMIN"));
    }

    private UserResponseDto getUpdatedAdminDto() {
        return new UserResponseDto(
                3L,
                "admin",
                "admin@example.com",
                "John",
                "Smith",
                Set.of("USER", "ADMIN")
        );
    }

    private UserResponseDto getUserResponseDto() {
        return new UserResponseDto(
                4L,
                "john.doe",
                "john.doe@example.com",
                "John",
                "Doe",
                Set.of("USER")
        );
    }

    private UserRegisterRequestDto getUpdateUserRequestDto() {
        return new UserRegisterRequestDto(
                "john.doe",
                "newpassword123",
                "newpassword123",
                "newemail@example.com",
                "John",
                "Doe"
        );
    }

    private UserResponseDto getUpdatedUserDto() {
        return new UserResponseDto(
                4L,
                "john.doe",
                "newemail@example.com",
                "John",
                "Doe",
                Set.of("USER")
        );
    }
}
