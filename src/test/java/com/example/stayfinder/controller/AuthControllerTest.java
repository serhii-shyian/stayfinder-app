package com.example.stayfinder.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.stayfinder.dto.user.UserLoginRequestDto;
import com.example.stayfinder.dto.user.UserLoginResponseDto;
import com.example.stayfinder.dto.user.UserRegisterRequestDto;
import com.example.stayfinder.dto.user.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {
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
            Authenticate user with valid credentials
            """)
    void login_ValidCredentials_ReturnsUserLoginResponseDto() throws Exception {
        // Given
        UserLoginRequestDto requestDto = getLoginRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isAccepted())
                .andReturn();

        // Then
        UserLoginResponseDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), UserLoginResponseDto.class);
        assertNotNull(actual);
        assertTrue(StringUtils.hasText(actual.token()));
    }

    @Test
    @Order(2)
    @DisplayName("""
            Authenticate user with invalid credentials
            """)
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        UserLoginRequestDto requestDto = getWrongPasswordRequest();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("""
            Register a new user with valid details
            """)
    void register_ValidUserDetails_ReturnsUserResponseDto() throws Exception {
        // Given
        UserRegisterRequestDto requestDto = getRegisterRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        UserResponseDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), UserResponseDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals("newuser@example.com", actual.email());
    }

    @Test
    @Order(4)
    @DisplayName("""
            Register a user with invalid details
            """)
    void register_InvalidUserDetails_ReturnsBadRequest() throws Exception {
        // Given
        UserRegisterRequestDto requestDto = getShortPasswordRequest();

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When
        mockMvc.perform(
                        post("/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    private UserLoginRequestDto getLoginRequestDto() {
        return new UserLoginRequestDto(
                "admin",
                "qwerty");
    }

    private UserRegisterRequestDto getRegisterRequestDto() {
        return new UserRegisterRequestDto(
                "newuser",
                "password123",
                "password123",
                "newuser@example.com",
                "New",
                "User");
    }

    private UserLoginRequestDto getWrongPasswordRequest() {
        return new UserLoginRequestDto(
                "admin",
                "wrongpassword");
    }

    private UserRegisterRequestDto getShortPasswordRequest() {
        return new UserRegisterRequestDto(
                "newuser",
                "short",
                "short",
                "invalid-email",
                "",
                "");
    }
}
