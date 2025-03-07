package com.example.stayfinder.service.notification;

import com.example.stayfinder.model.Accommodation;
import com.example.stayfinder.model.Booking;
import com.example.stayfinder.model.Payment;
import com.example.stayfinder.model.User;
import java.util.List;
import java.util.Set;

public interface NotificationService {
    void sendCreateBookingMessage(
            Accommodation accommodation, User user, Booking booking);

    void sendCancelBookingMessage(User user, Booking booking);

    void sendCreateAccommodationMessage(Accommodation accommodation, User user);

    void sendReleaseAccommodationMessage(Set<Long> accommodationIds, List<User> userList);

    void sendSuccessPaymentMessage(Payment payment);
}
