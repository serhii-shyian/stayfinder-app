package com.example.stayfinder.controller;

import static org.apache.commons.lang3.builder.EqualsBuilder.reflectionEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.stayfinder.dto.booking.BookingDto;
import com.example.stayfinder.dto.booking.CreateBookingRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingControllerTest {
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
            Get all bookings by authenticated user
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void getAllByAuthUserId_ReturnsBookingDtoList() throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        get("/bookings/my")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookingDto[].class);
        assertEquals(2, actual.length);
    }

    @Test
    @Order(2)
    @DisplayName("""
            Create a new booking with valid request
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void createBooking_ValidRequest_ReturnsBookingDto() throws Exception {
        // Given
        CreateBookingRequestDto requestDto = getCreateBookingRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        BookingDto expected = createBookingDto();

        // When
        MvcResult result = mockMvc.perform(
                        post("/bookings")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        BookingDto actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), BookingDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @Test
    @Order(3)
    @DisplayName("""
            Get a list of bookings by user id or status
            """)
    @WithUserDetails(value = "admin",
            userDetailsServiceBeanName = "customUserDetailsService")
    void getAllByUserIdAndStatus_ReturnsListOfBookings() throws Exception {
        // Given
        List<BookingDto> expectedBookings = List.of(getBookingDto());

        // When
        MvcResult result = mockMvc.perform(
                        get("/bookings")
                                .param("userIdArray", "4")
                                .param("statusArray", "CONFIRMED")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        List<BookingDto> actualBookings = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                objectMapper.getTypeFactory()
                        .constructCollectionType(List.class, BookingDto.class)
        );

        assertNotNull(actualBookings);
        assertEquals(expectedBookings.size(), actualBookings.size());

        for (int i = 0; i < expectedBookings.size(); i++) {
            assertTrue(reflectionEquals(expectedBookings.get(i), actualBookings.get(i), "id"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("""
            Get booking by id for authenticated user
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void getByAuthUserIdAndId_ValidId_ReturnsBookingDto() throws Exception {
        // Given
        Long bookingId = 2L;
        BookingDto expected = getBookingDto();

        // When
        MvcResult result = mockMvc.perform(
                        get("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), BookingDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @Order(5)
    @DisplayName("""
            Update booking by id for authenticated user
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void updateByAuthUserIdAndId_ValidRequest_ReturnsUpdatedBookingDto()
            throws Exception {
        // Given
        Long bookingId = 2L;
        CreateBookingRequestDto requestDto = getCreateBookingRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        BookingDto expected = getBookingDto();

        // When
        MvcResult result = mockMvc.perform(
                        put("/bookings/{id}", bookingId)
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), BookingDto.class);
        assertEquals(expected, actual);
    }

    @Test
    @Order(6)
    @DisplayName("""
            Cancel booking by id for authenticated user
            """)
    @WithUserDetails(value = "john.doe",
            userDetailsServiceBeanName = "customUserDetailsService")
    void cancelByAuthUserIdAndId_ValidId_ReturnsNoContent() throws Exception {
        // Given
        Long bookingId = 2L;

        // When
        MvcResult result = mockMvc.perform(
                        delete("/bookings/{id}", bookingId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.isEmpty());
    }

    private CreateBookingRequestDto getCreateBookingRequestDto() {
        return new CreateBookingRequestDto(
                LocalDateTime.of(
                        2025, 2, 20, 14, 0),
                LocalDateTime.of(
                        2025, 2, 25, 11, 0),
                2L
        );
    }

    private BookingDto getBookingDto() {
        CreateBookingRequestDto requestDto = getCreateBookingRequestDto();
        return new BookingDto(
                2L,
                requestDto.checkInDate(),
                requestDto.checkOutDate(),
                requestDto.accommodationId(),
                "John Doe",
                "CONFIRMED"
        );
    }

    private BookingDto createBookingDto() {
        return new BookingDto(
                1L,
                LocalDateTime.of(
                        2025, 2, 20, 14, 0),
                LocalDateTime.of(
                        2025, 2, 25, 11, 0),
                2L,
                "John Doe",
                "PENDING"
        );
    }
}
