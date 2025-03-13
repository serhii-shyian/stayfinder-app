package com.example.stayfinder.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.stayfinder.config.StripeConfig;
import com.example.stayfinder.dto.payment.PaymentDto;
import com.example.stayfinder.dto.payment.PaymentLowInfoDto;
import com.example.stayfinder.dto.payment.PaymentWithoutSessionDto;
import com.example.stayfinder.exception.DataProcessingException;
import com.example.stayfinder.exception.EntityNotFoundException;
import com.example.stayfinder.mapper.PaymentMapper;
import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.Payment;
import com.example.stayfinder.repository.booking.BookingRepository;
import com.example.stayfinder.repository.payment.PaymentRepository;
import com.example.stayfinder.service.notification.NotificationService;
import com.example.stayfinder.service.payment.StripePaymentService;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    @InjectMocks
    private StripePaymentService stripePaymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private StripeConfig stripeConfig;
    @Mock
    private NotificationService notificationService;
    @Mock
    private Session session;

    @Test
    @DisplayName("""
            Find all payments by user ID when payments exist
            """)
    void findAllPaymentsByUserId_ExistingPayments_ReturnsPaymentList() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Payment payment1 = getPayment();
        Payment payment2 = getPayment();
        List<Payment> paymentsList = List.of(payment1, payment2);
        Page<Payment> paymentsPage = new PageImpl<>(paymentsList);
        PaymentLowInfoDto dto1 = getPaymentLowInfoDto(payment1);
        PaymentLowInfoDto dto2 = getPaymentLowInfoDto(payment2);

        when(paymentRepository.findByBookingUserId(1L, pageable))
                .thenReturn(paymentsPage);
        when(paymentMapper.toLowInfoDto(payment1)).thenReturn(dto1);
        when(paymentMapper.toLowInfoDto(payment2)).thenReturn(dto2);

        List<PaymentLowInfoDto> expected = List.of(dto1, dto2);

        // When
        Page<PaymentLowInfoDto> actual = stripePaymentService.findAllByBookingUserId(1L, pageable);

        // Then
        assertEquals(expected, actual.getContent());
        verify(paymentRepository).findByBookingUserId(1L, pageable);
        verify(paymentMapper).toLowInfoDto(payment1);
        verify(paymentMapper).toLowInfoDto(payment2);
        verifyNoMoreInteractions(paymentRepository, paymentMapper);
    }

    @Test
    @DisplayName("""
            Find all payments by user ID when no payments exist
            """)
    void findAllPaymentsByUserId_NoPayments_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        when(paymentRepository.findByBookingUserId(99L, pageable))
                .thenReturn(Page.empty());

        // Then
        assertThrows(EntityNotFoundException.class,
                () -> stripePaymentService.findAllByBookingUserId(99L, pageable));
        verify(paymentRepository).findByBookingUserId(99L, pageable);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("""
            Create Stripe session successfully
            """)
    void createSession_ValidBookingId_ReturnsPaymentDto() {
        // Given
        Booking booking = getBooking();
        Payment payment = getPayment();
        PaymentDto expected = getPaymentDto(payment);
        BigDecimal totalAmount = BigDecimal.valueOf(600);
        SessionCreateParams params = getSessionCreateParams();
        Session session = getSession();
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(stripeConfig.createSessionParams(totalAmount)).thenReturn(params);
            mockedSession.when(() -> Session.create(params)).thenReturn(session);
            when(paymentRepository.save(paymentCaptor.capture())).thenReturn(payment);
            when(paymentMapper.toDto(payment)).thenReturn(expected);

            // When
            PaymentDto actual = stripePaymentService.createSession(1L);

            // Then
            assertEquals(expected, actual);
            Payment capturedPayment = paymentCaptor.getValue();
            assertEquals(payment.getSessionId(), capturedPayment.getSessionId());
            assertEquals(payment.getSessionUrl(), capturedPayment.getSessionUrl());
            assertEquals(payment.getAmount(), capturedPayment.getAmount());
            assertEquals(payment.getStatus(), capturedPayment.getStatus());
            verify(bookingRepository).findById(1L);
            verify(stripeConfig).createSessionParams(totalAmount);
            verify(paymentRepository).save(paymentCaptor.capture());
            verify(paymentMapper).toDto(payment);
            verifyNoMoreInteractions(
                    bookingRepository, stripeConfig, paymentRepository, paymentMapper);
        }
    }

    @Test
    @DisplayName("""
            Create Stripe session fails when Stripe API throws exception
            """)
    void createSession_StripeException_ThrowsException() {
        // Given
        Booking booking = getBooking();
        BigDecimal totalAmount = BigDecimal.valueOf(600);
        SessionCreateParams params = getSessionCreateParams();

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
            when(stripeConfig.createSessionParams(totalAmount)).thenReturn(params);
            mockedSession.when(() -> Session.create(params)).thenThrow(
                    new InvalidRequestException(
                            "Invalid request", null, null, null, null, null));

            // When & Then
            assertThrows(DataProcessingException.class,
                    () -> stripePaymentService.createSession(1L));
            verify(bookingRepository).findById(1L);
            verify(stripeConfig).createSessionParams(totalAmount);
            verifyNoInteractions(paymentRepository, paymentMapper);
        }
    }

    @Test
    @DisplayName("""
            Process successful payment
            """)
    void processSuccessfulPayment_ValidSessionId_UpdatesPaymentAndBookingStatus() {
        // Given
        String validSessionId = "validSessionId";
        Payment payment = getPayment();
        payment.setStatus(Payment.PaymentStatus.PENDING);
        PaymentWithoutSessionDto expected = getPaymentWithoutSessionDto(payment);

        when(paymentRepository.findBySessionId(validSessionId))
                .thenReturn(Optional.of(payment));
        when(paymentMapper.toWithoutSessionDto(payment)).thenReturn(expected);

        // When
        PaymentWithoutSessionDto actual
                = stripePaymentService.processSuccessfulPayment(validSessionId);

        // Then
        assertEquals(expected, actual);
        verify(paymentRepository).updateStatus(
                payment.getId(), Payment.PaymentStatus.PAID);
        verify(paymentMapper).toWithoutSessionDto(payment);
        verify(bookingRepository).updateStatus(
                payment.getBooking().getId(), Booking.Status.CONFIRMED);
        verify(notificationService).sendSuccessPaymentMessage(payment);
    }

    @Test
    @DisplayName("""
            Process successful payment fails for invalid sessionId
            """)
    void processSuccessfulPayment_InvalidSessionId_ThrowsException() {
        // Given
        String invalidSessionId = "invalidSessionId";

        when(paymentRepository.findBySessionId(invalidSessionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> {
            stripePaymentService.processSuccessfulPayment(invalidSessionId);
        });
        verify(paymentRepository).findBySessionId(invalidSessionId);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("""
            Process cancel payment successfully for a valid sessionId
            """)
    void processCancelPayment_ValidSessionId_UpdatesStatusesAndReturnsMessage() {
        // Given
        String sessionId = "session_123";
        Payment payment = getPayment();
        Booking booking = getBooking();
        payment.setId(booking.getId());

        String expectedMessage = String.format("The payment for session ID '%s' "
                + "has been canceled and can be made later.", sessionId);

        // Mocking repository method
        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.of(payment));

        // When
        String actualMessage = stripePaymentService.processCancelPayment(sessionId);

        // Then
        assertEquals(expectedMessage, actualMessage);
        verify(paymentRepository, times(2)).findBySessionId(sessionId);
    }

    @Test
    @DisplayName("""
            Process cancel payment fails when sessionId is not found
            """)
    void processCancelPayment_InvalidSessionId_ThrowsException() {
        // Given
        String sessionId = "invalid_session_456";

        when(paymentRepository.findBySessionId(sessionId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class,
                () -> stripePaymentService.processCancelPayment(sessionId));
        verify(paymentRepository).findBySessionId(sessionId);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    @DisplayName("""
            Check expired payments updates pending payments to expired
            """)
    void checkExpiredPayments_UpdatesPendingPaymentsToExpired() {
        // Given
        Long currentTimestamp = System.currentTimeMillis() / 1000;

        doNothing().when(paymentRepository).updateExpiredPayments(
                currentTimestamp,
                Payment.PaymentStatus.EXPIRED,
                Payment.PaymentStatus.PENDING);

        // When
        stripePaymentService.checkExpiredPayments();

        // Then
        verify(paymentRepository).updateExpiredPayments(
                eq(currentTimestamp),
                eq(Payment.PaymentStatus.EXPIRED),
                eq(Payment.PaymentStatus.PENDING));
        verifyNoMoreInteractions(paymentRepository);
    }

    private Payment getPayment() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-03-01T00:00:00Z"),
                ZoneOffset.UTC);
        return new Payment()
                .setId(1L)
                .setBooking(getBooking())
                .setSessionId("session_123")
                .setSessionUrl("http://example.com/session_123")
                .setExpiredTime(fixedClock.millis() + 3600 * 1000)
                .setAmount(BigDecimal.valueOf(600))
                .setStatus(Payment.PaymentStatus.PENDING);
    }

    private Booking getBooking() {
        return new Booking()
                .setId(1L)
                .setCheckInDate(LocalDateTime.of(
                        2025, 3, 1, 14, 0))
                .setCheckOutDate(LocalDateTime.of(
                        2025, 3, 7, 11, 0))
                .setStatus(Booking.Status.PENDING)
                .setAccommodation(getAccommodation());
    }

    private Accommodation getAccommodation() {
        return new Accommodation()
                .setId(1L)
                .setDailyRate(BigDecimal.valueOf(100));
    }

    private PaymentLowInfoDto getPaymentLowInfoDto(Payment payment) {
        return new PaymentLowInfoDto(
                payment.getId(),
                payment.getSessionId(),
                payment.getAmount(),
                payment.getStatus().name());
    }

    private PaymentDto getPaymentDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getSessionId(),
                payment.getSessionUrl(),
                payment.getAmount(),
                payment.getStatus().name());
    }

    private PaymentWithoutSessionDto getPaymentWithoutSessionDto(Payment payment) {
        return new PaymentWithoutSessionDto(
                payment.getBooking().getId(),
                payment.getStatus().name(),
                payment.getAmount());
    }

    private SessionCreateParams getSessionCreateParams() {
        return SessionCreateParams.builder().build();
    }

    private Session getSession() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2025-03-01T00:00:00Z"),
                ZoneOffset.UTC);
        Session session = new Session();
        session.setId("session_123");
        session.setUrl("http://example.com/session_123");
        session.setExpiresAt(fixedClock.millis() + 3600 * 1000);
        return session;
    }
}
