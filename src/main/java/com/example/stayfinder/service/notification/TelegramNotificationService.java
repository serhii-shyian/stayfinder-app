package com.example.stayfinder.service.notification;

import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.Payment;
import com.example.stayfinder.model.User;
import com.example.stayfinder.service.telegram.TelegramBot;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final TelegramBot telegramBot;

    @Async
    @Override
    public void sendCreateBookingMessage(Accommodation accommodation, User user, Booking booking) {
        telegramBot.sendNotification(
                formatMessage(NotificationTemplates.BOOKING_CREATED_TEMPLATE,
                        booking.getId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        accommodation.getId(),
                        user.getId(),
                        getUserName(user)),
                user.getId());
    }

    @Async
    @Override
    public void sendCancelBookingMessage(User user, Booking booking) {
        telegramBot.sendNotification(
                formatMessage(NotificationTemplates.BOOKING_CANCELED_TEMPLATE,
                        booking.getId(),
                        user.getId(),
                        getUserName(user)),
                user.getId());
    }

    @Async
    @Override
    public void sendCreateAccommodationMessage(Accommodation accommodation, User user) {
        telegramBot.sendNotification(
                formatMessage(NotificationTemplates.ACCOMMODATION_CREATED_MESSAGE,
                        accommodation.getId(),
                        accommodation.getType(),
                        accommodation.getSize(),
                        accommodation.getDailyRate()), user.getId());
    }

    @Async
    @Override
    public void sendReleaseAccommodationMessage(Set<Long> accommodationIds, List<User> userList) {
        telegramBot.sendNotificationToUsers(
                formatMessage(NotificationTemplates.ACCOMMODATION_RELEASE_MESSAGE,
                        accommodationIds.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", "))),
                userList);
    }

    @Async
    @Override
    public void sendSuccessPaymentMessage(Payment payment) {
        telegramBot.sendNotification(
                formatMessage(NotificationTemplates.PAYMENT_SUCCESS_MESSAGE,
                        payment.getId(),
                        payment.getBooking().getId(),
                        payment.getStatus().toString(),
                        payment.getAmount()),
                payment.getBooking().getUser().getId());
    }

    private String getUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String formatMessage(String template, Object... args) {
        return String.format(template, args);
    }
}
