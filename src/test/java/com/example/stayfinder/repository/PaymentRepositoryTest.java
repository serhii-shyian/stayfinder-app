package com.example.stayfinder.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.stayfinder.model.Payment;
import com.example.stayfinder.repository.payment.PaymentRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

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
            Find payment by sessionId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql",
            "classpath:database/payments/insert-into-payments.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/payments/delete-all-from-payments.sql",
            "classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findBySessionId_ExistingSessionId_ReturnsPayment() {
        // Given
        String sessionId = "session-12345";

        // When
        Optional<Payment> optionalPayment = paymentRepository.findBySessionId(sessionId);

        // Then
        assertTrue(optionalPayment.isPresent());
        assertEquals(sessionId, optionalPayment.get().getSessionId());
    }

    @Test
    @DisplayName("""
            Find payment by non-existing sessionId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql",
            "classpath:database/payments/insert-into-payments.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/payments/delete-all-from-payments.sql",
            "classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findBySessionId_NonExistingSessionId_ReturnsEmptyOptional() {
        // Given
        String sessionId = "non-existing-session";

        // When
        Optional<Payment> optionalPayment = paymentRepository.findBySessionId(sessionId);

        // Then
        assertFalse(optionalPayment.isPresent());
    }

    @Test
    @DisplayName("""
            Find payments by userId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql",
            "classpath:database/payments/insert-into-payments.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/payments/delete-all-from-payments.sql",
            "classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByBookingUserId_ExistingUserId_ReturnsPaymentPage() {
        // Given
        Long userId = 4L;
        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<Payment> paymentPage = paymentRepository.findByBookingUserId(
                userId, pageable);

        // Then
        assertFalse(paymentPage.isEmpty());
        assertEquals(2, paymentPage.getTotalElements());
        for (Payment payment : paymentPage.getContent()) {
            assertEquals(userId, payment.getBooking().getUser().getId());
        }
    }

    @Test
    @DisplayName("""
            Find payments by non-existing userId
            """)
    @Sql(scripts = {"classpath:database/roles/insert-into-roles.sql",
            "classpath:database/users/insert-into-users.sql",
            "classpath:database/accoms/insert-into-accommodations.sql",
            "classpath:database/bookings/insert-into-bookings.sql",
            "classpath:database/payments/insert-into-payments.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/payments/delete-all-from-payments.sql",
            "classpath:database/bookings/delete-all-from-bookings.sql",
            "classpath:database/accoms/delete-all-from-accommodations.sql",
            "classpath:database/users/delete-all-from-users.sql",
            "classpath:database/roles/delete-all-from-roles.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByBookingUserId_NonExistingUserId_ReturnsEmptyPage() {
        // Given
        Long userId = 99L;
        Pageable pageable = PageRequest.of(0, 5);

        // When
        Page<Payment> paymentPage = paymentRepository.findByBookingUserId(
                userId, pageable);

        // Then
        assertTrue(paymentPage.isEmpty());
    }
}
