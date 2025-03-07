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

import com.example.stayfinder.dto.accommodation.AccommodationDto;
import com.example.stayfinder.dto.accommodation.AccommodationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
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
class AccommodationControllerTest {
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
            Get list of all accommodations when they exist
            """)
    @WithMockUser(username = "user")
    void getAllAccommodations_AccommodationsExist_ReturnsAccommodationDtoList()
            throws Exception {
        // Given
        List<AccommodationDto> expected = getAccommodationDtoList();

        // When
        MvcResult result = mockMvc.perform(
                        get("/accommodations")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        AccommodationDto[] actual = objectMapper.readValue(
                result.getResponse()
                        .getContentAsByteArray(), AccommodationDto[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @Order(2)
    @DisplayName("""
            Get accommodation by id when it exists
            """)
    @WithMockUser(username = "user")
    void getById_ExistingAccommodationId_ReturnsAccommodationDto()
            throws Exception {
        // Given
        AccommodationDto expected = getAccommodationDtoList().get(0);

        // When
        MvcResult result = mockMvc.perform(
                        get("/accommodations/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        // Then
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), AccommodationDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    @DisplayName("""
            Create a new accommodation from valid DTO
            """)
    @WithUserDetails(value = "admin",
            userDetailsServiceBeanName = "customUserDetailsService")
    void createAccommodation_ValidRequestDto_ReturnsAccommodationDto()
            throws Exception {
        // Given
        AccommodationRequestDto requestDto
                = getCreateAccommodationRequestDtoList().get(0);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        AccommodationDto expected = getAccommodationDtoFromRequestDto(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        post("/accommodations")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        // Then
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), AccommodationDto.class);
        assertNotNull(actual);
        assertNotNull(actual.id());
        assertTrue(reflectionEquals(expected, actual, "id"));
    }

    @Test
    @Order(4)
    @DisplayName("""
            Update accommodation by id when it exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateAccommodation_ExistingAccommodationId_ReturnsAccommodationDto()
            throws Exception {
        // Given
        AccommodationRequestDto requestDto
                = getCreateAccommodationRequestDtoList().get(1);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        AccommodationDto expected = getAccommodationDtoFromRequestDto(requestDto);

        // When
        MvcResult result = mockMvc.perform(
                        put("/accommodations/2")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), AccommodationDto.class);
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    @Order(5)
    @DisplayName("""
            Delete accommodation by id when it exists
            """)
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void deleteAccommodation_ExistingAccommodationId_ReturnsNothing()
            throws Exception {
        // When
        MvcResult result = mockMvc.perform(
                        delete("/accommodations/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andReturn();

        // Then
        String actual = result.getResponse().getContentAsString();
        assertTrue(actual.isEmpty());
    }

    private List<AccommodationDto> getAccommodationDtoList() {
        return List.of(
                new AccommodationDto(
                        2L,
                        "APARTMENT",
                        "Downtown",
                        "1000 sqft",
                        Set.of(),
                        BigDecimal.valueOf(120.00)
                                .setScale(2, RoundingMode.HALF_UP),
                        5),
                new AccommodationDto(
                        3L,
                        "HOUSE",
                        "Suburbs",
                        "2000 sqft",
                        Set.of(),
                        BigDecimal.valueOf(200.00)
                                .setScale(2, RoundingMode.HALF_UP),
                        3));
    }

    private List<AccommodationRequestDto> getCreateAccommodationRequestDtoList() {
        return List.of(
                new AccommodationRequestDto(
                        "VACATION_HOME",
                        "Mountain Area",
                        "3000 sqft",
                        Set.of("WIFI", "FIREPLACE"),
                        BigDecimal.valueOf(250.00),
                        2),
                new AccommodationRequestDto(
                        "CONDO",
                        "Beachfront",
                        "1500 sqft",
                        Set.of("WIFI", "PARKING"),
                        BigDecimal.valueOf(180.00),
                        1)
        );
    }

    private AccommodationDto getAccommodationDtoFromRequestDto(
            AccommodationRequestDto requestDto) {
        return new AccommodationDto(
                2L,
                requestDto.type(),
                requestDto.location(),
                requestDto.size(),
                requestDto.amenities(),
                requestDto.dailyRate(),
                requestDto.availability());
    }
}
