package com.example.stayfinder.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Address;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.Payment;
import com.example.stayfinder.model.User;
import com.example.stayfinder.service.notification.NotificationTemplates;
import com.example.stayfinder.service.notification.TelegramNotificationService;
import com.example.stayfinder.service.telegram.TelegramBot;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceTest {
    @InjectMocks
    private TelegramNotificationService telegramNotificationService;
    @Mock
    private TelegramBot telegramBot;

    @Test
    @DisplayName("""
            Send booking create message
            """)
    public void sendBookingCreateMessage_ValidBooking_SendsNotification() {
        // Given
        Accommodation accommodation = getAccommodation();
        User user = getUser();
        Booking booking = getBooking(accommodation, user);
        String expectedMessage = String.format(
                NotificationTemplates.BOOKING_CREATED_TEMPLATE,
                booking.getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                accommodation.getId(),
                user.getId(),
                getUserName(user));

        // When
        telegramNotificationService.sendCreateBookingMessage(
                accommodation, user, booking);

        // Then
        verify(telegramBot).sendNotification(expectedMessage, user.getId());
        verifyNoMoreInteractions(telegramBot);
    }

    @Test
    @DisplayName("""
            Send booking canceled message
            """)
    public void sendBookingCanceledMessage_ValidBooking_SendsNotification() {
        // Given
        User user = getUser();
        Booking booking = getBooking(null, user);
        String expectedMessage = String.format(
                NotificationTemplates.BOOKING_CANCELED_TEMPLATE,
                booking.getId(),
                user.getId(),
                getUserName(user));

        // When
        telegramNotificationService.sendCancelBookingMessage(user, booking);

        // Then
        verify(telegramBot).sendNotification(expectedMessage, user.getId());
        verifyNoMoreInteractions(telegramBot);
    }

    @Test
    @DisplayName("""
            Send accommodation create message
            """)
    public void sendAccommodationCreateMessage_ValidAccommodation_SendsNotification() {
        // Given
        Accommodation accommodation = getAccommodation();
        User user = getUser();
        String expectedMessage = String.format(
                NotificationTemplates.ACCOMMODATION_CREATED_MESSAGE,
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getSize(),
                accommodation.getDailyRate());

        // When
        telegramNotificationService.sendCreateAccommodationMessage(
                accommodation, user);

        // Then
        verify(telegramBot).sendNotification(expectedMessage, user.getId());
        verifyNoMoreInteractions(telegramBot);
    }

    @Test
    @DisplayName("""
            Send accommodation release message
            """)
    public void sendAccommodationReleaseMessage_ValidAccommodationIds_SendsNotifications() {
        // Given
        Set<Long> accommodationIds = Set.of(1L, 2L, 3L);
        List<User> userList = List.of(getUser(), getAnotherUser());
        String expectedMessage = String.format(
                NotificationTemplates.ACCOMMODATION_RELEASE_MESSAGE,
                accommodationIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));

        // When
        telegramNotificationService.sendReleaseAccommodationMessage(
                accommodationIds, userList);

        // Then
        verify(telegramBot).sendNotificationToUsers(expectedMessage, userList);
        verifyNoMoreInteractions(telegramBot);
    }

    @Test
    @DisplayName("""
            Send payment success message
            """)
    public void sendPaymentSuccessMessage_ValidPayment_SendsNotification() {
        // Given
        Payment payment = getPayment();
        String expectedMessage = String.format(
                NotificationTemplates.PAYMENT_SUCCESS_MESSAGE,
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus().toString(),
                payment.getAmount());

        // When
        telegramNotificationService.sendSuccessPaymentMessage(payment);

        // Then
        verify(telegramBot).sendNotification(
                expectedMessage, payment.getBooking().getUser().getId());
        verifyNoMoreInteractions(telegramBot);
    }

    private Address createAddress() {
        return new Address()
                .setAddress("City Center");
    }

    private Accommodation getAccommodation() {
        return new Accommodation()
                .setId(1L)
                .setType(Accommodation.Type.HOUSE)
                .setLocation(createAddress())
                .setSize("Large")
                .setAmenities(Set.of(
                        Accommodation.Amenities.AIR_CONDITIONING,
                        Accommodation.Amenities.WIFI))
                .setDailyRate(BigDecimal.valueOf(150.0))
                .setAvailability(10);
    }

    private User getUser() {
        return new User()
                .setId(1L)
                .setUsername("john.doe")
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("john.doe@example.com");
    }

    private User getAnotherUser() {
        return new User()
                .setId(2L).setUsername("jane.doe")
                .setFirstName("Jane")
                .setLastName("Doe")
                .setEmail("jane.doe@example.com");
    }

    private Booking getBooking(Accommodation accommodation, User user) {
        return new Booking()
                .setId(1L)
                .setCheckInDate(LocalDateTime.now())
                .setCheckOutDate(LocalDateTime.now().plusDays(2))
                .setStatus(Booking.Status.PENDING)
                .setAccommodation(accommodation)
                .setUser(user);
    }

    private Payment getPayment() {
        Booking booking = getBooking(getAccommodation(), getUser());
        return new Payment()
                .setId(1L)
                .setBooking(booking)
                .setSessionId("session_123")
                .setSessionUrl("http://example.com/session_123")
                .setExpiredTime(System.currentTimeMillis() + 3600 * 1000)
                .setAmount(BigDecimal.valueOf(600))
                .setStatus(Payment.PaymentStatus.PAID);
    }

    private String getUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
