package com.example.stayfinder.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.stayfinder.dto.payment.CreatePaymentSessionDto;
import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
class PaymentControllerTest {
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
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/accoms/insert-into-accommodations.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/bookings/insert-into-bookings.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/payments/insert-into-payments.sql"));
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
                    new ClassPathResource("database/payments/delete-all-from-payments.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/bookings/delete-all-from-bookings.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/accoms/delete-all-from-accommodations.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/users/delete-all-from-users.sql"));
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/roles/delete-all-from-roles.sql"));
        }
    }

    @Test
    @Order(1)
    @DisplayName("""
            Get all payments by booking user id
            """)
    @WithUserDetails(value = "admin",
            userDetailsServiceBeanName = "customUserDetailsService")
    void getAllByBookingUserId_ReturnsPaymentLowInfoDtoPage() throws Exception {
        // Given
        long userId = 4L;
        List<PaymentLowInfoDto> expected = getPaymentLowInfoDtoList();

        // When
        MvcResult result = mockMvc.perform(
                        get("/payments")
                                .param("user_id", Long.toString(userId))
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = root.path("content");
        PaymentLowInfoDto[] actual = objectMapper.treeToValue(
                contentNode, PaymentLowInfoDto[].class);

        assertNotNull(actual);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.asList(actual));
    }

    @Test
    @Order(2)
    @DisplayName("""
            Create a payment session with valid booking id
            """)
    @WithMockUser(username = "john.doe")
    void createSession_ValidRequest_ReturnsPaymentDto() throws Exception {
        //Given
        CreatePaymentSessionDto requestDto = getCreatePaymentSessionDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        //When
        MvcResult result = mockMvc.perform(
                        post("/payments")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        PaymentDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), PaymentDto.class);

        assertNotNull(actual);
        assertNotNull(actual.id());
        assertEquals(requestDto.bookingId(), actual.bookingId());
        assertEquals(
                BigDecimal.valueOf(600.00)
                        .setScale(2, RoundingMode.HALF_UP),
                actual.amount());
        assertEquals("PENDING", actual.status());
        assertNotNull(actual.sessionId());
        assertTrue(actual.sessionId().startsWith("cs_"));
        assertNotNull(actual.sessionUrl());
        assertTrue(actual.sessionUrl().contains("https://checkout.stripe.com/c/pay/"));
    }

    @Test
    @Order(3)
    @DisplayName("""
            Handle success payment
            """)
    @WithMockUser(username = "john.doe")
    void handleSuccessPayment_ValidSessionId_ReturnsPaymentWithoutSessionDto()
            throws Exception {
        //Given
        String sessionId = "session-12345";
        PaymentWithoutSessionDto expected = getPaymentWithoutSessionDto();

        //When
        MvcResult result = mockMvc.perform(
                        get("/payments/success")
                                .param("sessionId", sessionId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        PaymentWithoutSessionDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), PaymentWithoutSessionDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Order(4)
    @DisplayName("""
            Handle canceled payment
            """)
    @WithMockUser(username = "john.doe")
    void handleCancelledPayment_ValidSessionId_ReturnsConfirmation() throws Exception {
        //Given
        String sessionId = "session-67890";

        //When
        MvcResult result = mockMvc.perform(
                        get("/payments/cancel")
                                .param("sessionId", sessionId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        String actual = result.getResponse().getContentAsString();
        assertNotNull(actual);
        assertEquals(String.format("The payment for session ID '%s' "
                + "has been canceled and can be made later.", sessionId), actual);
    }

    private List<PaymentLowInfoDto> getPaymentLowInfoDtoList() {
        return List.of(
                new PaymentLowInfoDto(2L,
                        "session-12345",
                        BigDecimal.valueOf(120.00),
                        "PENDING"),
                new PaymentLowInfoDto(3L,
                        "session-67890",
                        BigDecimal.valueOf(200.00),
                        "PAID")
        );
    }

    private CreatePaymentSessionDto getCreatePaymentSessionDto() {
        return new CreatePaymentSessionDto(2L);
    }

    private PaymentWithoutSessionDto getPaymentWithoutSessionDto() {
        return new PaymentWithoutSessionDto(
                2L,
                "PAID",
                BigDecimal.valueOf(120.00)
                        .setScale(2, RoundingMode.HALF_UP));
    }
}
