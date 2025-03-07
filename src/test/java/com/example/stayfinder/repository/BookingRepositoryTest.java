package com.example.stayfinder.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.User;
import com.example.stayfinder.repository.booking.BookingRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(connection,
                    new ClassPathResource("database/delete-all-data-before-tests.sql"));
        }
    }

    @Test
    @DisplayName("""
            Find all bookings for a user by userId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserId_ExistingUserId_ReturnsBookingList() {
        // Given
        Long userId = 4L;
        List<Booking> expected = List.of(
                getBookingList().get(0), getBookingList().get(1));

        // When
        List<Booking> actual = bookingRepository.findByUserId(
                userId, Pageable.ofSize(5)).getContent();

        // Then
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("accommodation", "user")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("""
            Find booking by userId and bookingId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserIdAndId_ExistingUserIdAndBookingId_ReturnsBooking() {
        // Given
        Long expectedUserId = 4L;
        Long expectedBookingId = 2L;

        // When
        Optional<Booking> actual = bookingRepository.findByUserIdAndId(
                expectedUserId, expectedBookingId);

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expectedBookingId, actual.get().getId());
        assertEquals(expectedUserId, actual.get().getUser().getId());
    }

    @Test
    @DisplayName("""
            Find bookings by accommodationId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByAccommodationId_ExistingAccommodationId_ReturnsBookingList() {
        // Given
        Long accId = 2L;

        // When
        List<Booking> bookings = bookingRepository.findByAccommodationId(accId);

        // Then
        assertFalse(bookings.isEmpty());
        assertEquals(2, bookings.size());
        for (Booking booking : bookings) {
            assertEquals(accId, booking.getAccommodation().getId());
        }
    }

    @Test
    @DisplayName("""
            Find bookings by non-existing accommodationId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByAccommodationId_NonExistingAccommodationId_ReturnsEmptyList() {
        // Given
        Long accId = 99L;

        // When
        List<Booking> bookings = bookingRepository.findByAccommodationId(accId);

        // Then
        assertTrue(bookings.isEmpty());
    }

    @Test
    @DisplayName("""
            Find booking by non-existing userId and bookingId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByUserIdAndId_NonExistingUserIdAndBookingId_ReturnsEmptyOptional() {
        // Given
        Long expectedUserId = 99L;
        Long expectedBookingId = 99L;

        // When
        Optional<Booking> actual = bookingRepository.findByUserIdAndId(
                expectedUserId, expectedBookingId);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    @DisplayName("""
            Returns bookings with a check-out time and no specified status
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByCheckOutDateAndStatusNot_ExistingDateAndStatus_ReturnsBookingList() {
        // Expected
        LocalDateTime expectedDate = LocalDateTime.of(
                2025, 4, 15, 11, 0, 0);
        Booking.Status expectedStatusNot = Booking.Status.CANCELED;

        // When
        List<Booking> actualBookings = bookingRepository.findByCheckOutDateAndStatusNot(
                expectedDate, expectedStatusNot);

        // Then
        for (Booking actualBooking : actualBookings) {
            assertEquals(expectedDate, actualBooking.getCheckOutDate(),
                    "Check-out date should match the expected date.");
            assertNotEquals(expectedStatusNot, actualBooking.getStatus(),
                    "Booking status should not be the 'CANCELED' status.");
        }
    }

    @Test
    @DisplayName("""
            Returns an empty list when no bookings match the date and status
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByCheckOutDateAndStatusNot_NotExistingDate_ReturnsEmptyList() {
        // Given
        LocalDateTime givenDate = LocalDateTime.of(
                2025, 1, 17, 10, 0, 0);
        Booking.Status givenStatus = Booking.Status.CANCELED;
        List<Booking> expected = Collections.emptyList();

        // When
        List<Booking> actual = bookingRepository.findByCheckOutDateAndStatusNot(
                givenDate, givenStatus);

        // Then
        assertEquals(expected, actual);
    }

    private List<Booking> getBookingList() {
        return List.of(
                new Booking()
                        .setId(2L)
                        .setCheckInDate(LocalDateTime.of(
                                2025, 2, 20, 14, 0))
                        .setCheckOutDate(LocalDateTime.of(
                                2025, 2, 25, 11, 0))
                        .setStatus(Booking.Status.CONFIRMED)
                        .setAccommodation(getAccommodationList().get(0))
                        .setUser(getUserList().get(0)),
                new Booking()
                        .setId(3L)
                        .setCheckInDate(LocalDateTime.of(
                                2025, 3, 1, 14, 0))
                        .setCheckOutDate(LocalDateTime.of(
                                2025, 3, 5, 11, 0))
                        .setStatus(Booking.Status.PENDING)
                        .setAccommodation(getAccommodationList().get(0))
                        .setUser(getUserList().get(0))
        );
    }

    private Address createAddress() {
        return new Address()
                .setAddress("Downtown");
    }

    private List<Accommodation> getAccommodationList() {
        return List.of(
                new Accommodation()
                        .setId(1L)
                        .setType(Accommodation.Type.APARTMENT)
                        .setLocation(createAddress())
                        .setSize("1000 sqft")
                        .setDailyRate(BigDecimal.valueOf(120.00))
                        .setAvailability(5)
        );
    }

    private List<User> getUserList() {
        return List.of(
                new User()
                        .setId(2L)
                        .setUsername("john.doe")
                        .setEmail("john.doe@example.com")
                        .setFirstName("John")
                        .setLastName("Doe")
        );
    }
}
